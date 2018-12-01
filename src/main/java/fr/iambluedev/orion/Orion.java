package fr.iambluedev.orion;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import fr.iambluedev.orion.config.Config;
import fr.iambluedev.orion.db.Rethink;
import fr.iambluedev.orion.listener.DataListener;
import fr.iambluedev.orion.listener.WebsiteListener;
import fr.iambluedev.orion.manager.ConfigManager;
import fr.iambluedev.orion.manager.LoadWebsitesManager;
import fr.iambluedev.orion.manager.CrawlerManager;
import fr.iambluedev.orion.manager.WebsiteManager;
import fr.iambluedev.orion.task.QueueTask;
import fr.iambluedev.orion.web.Spark;
import fr.skybeastmc.events.EventManager;
import lombok.Getter;

@Getter
public class Orion {

	private CrawlerManager scrawlerManager;
	private LoadWebsitesManager loadWebsiteManager;
	private WebsiteManager websiteManager;
	private Config config;
	private Rethink db;
	
	private ScheduledExecutorService scheduler;
	
	private boolean running;
	private static Orion instance;
	
	private final static Logger logger = Logger.getLogger(Orion.class);

	private Orion() {}

	public static Orion getInstance() {
		if (instance == null) {
			instance = new Orion();
		}

		return instance;
	}

	public void start() {
		this.running = true;
		logger.info("Starting App");

		try {
			this.config = ConfigManager.loadConfig();
		} catch (IOException e) {
			logger.error("Unable to load config file, please check the stacktrace below and restart later");
			e.printStackTrace();
			this.stop();
		}
		
		this.scheduler = Executors.newScheduledThreadPool(4);
		
		logger.info("Starting connection to db");
		this.db = new Rethink(this.config);
		
		logger.info("Loading listener");
		EventManager.registerListener(new DataListener());
		EventManager.registerListener(new WebsiteListener());

		logger.info("Loading scrawler manager");
		this.scrawlerManager = CrawlerManager.getInstance();
		
		logger.info("Loading websites manager");
		this.websiteManager = WebsiteManager.getInstance();

		logger.info("Loading websites");
		this.loadWebsiteManager = LoadWebsitesManager.getInstance();

		new Spark().setup(this.config.getPort());
		new QueueTask().start();
	}

	public void stop() {
		this.running = false;

		logger.info("Stopping webserver");
		spark.Spark.stop();

		logger.info("Bye Bye");
	}

	public void executeCommand(String command) {
		logger.info("Executed command : " + command);

		if (command.equals("/stop")) {
			this.stop();
		}
	}

}
