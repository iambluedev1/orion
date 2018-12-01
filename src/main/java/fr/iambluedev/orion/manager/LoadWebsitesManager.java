package fr.iambluedev.orion.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import fr.iambluedev.orion.Orion;
import fr.iambluedev.orion.event.WebsiteLoadedEvent;
import fr.iambluedev.orion.object.Action;
import fr.iambluedev.orion.object.Data;
import fr.iambluedev.orion.object.Link;
import fr.iambluedev.orion.object.Rule;
import fr.iambluedev.orion.object.Website;
import fr.iambluedev.orion.util.ExtractUtil;
import fr.skybeastmc.events.EventManager;
import lombok.Getter;

@Getter
public class LoadWebsitesManager {

	private static LoadWebsitesManager instance;

	private File folder;
	private final static Logger logger = Logger.getLogger(LoadWebsitesManager.class);

	private LoadWebsitesManager() {
		this.folder = new File("websites");

		if (!this.folder.exists()) {
			logger.info("Websites folder doesn't exist, so creating it...");
			this.folder.mkdir();
		} else if (!this.folder.isDirectory()) {
			logger.error("Websites folder exist but it's not a folder ! Please delete it and restart");
			Orion.getInstance().stop();
		} else {
			logger.info("Okey, it sounds good");
			try {
				this.loadWebsites();
			} catch (IOException e) {
				e.printStackTrace();
				Orion.getInstance().stop();
			}
		}
	}

	public static LoadWebsitesManager getInstance() {
		if (instance == null) {
			instance = new LoadWebsitesManager();
		}

		return instance;
	}

	private void loadWebsites() throws IOException {
		File[] files = this.folder.listFiles();
		logger.info("Loading " + files.length + " configurations");

		for (File file : files) {
			logger.info("Loading " + file.getName());

			InputStream is = new FileInputStream(file);
			String jsonTxt = IOUtils.toString(is, "UTF-8");

			Gson gson = new Gson();
			Website web = gson.fromJson(jsonTxt, Website.class);

			if (checkConfig(web)) {
				logger.info("Config " + file.getName() + " loaded successfully");

				if (WebsiteManager.getInstance().existWebsite(web)) {
					logger.error("Oh crap, config already loaded. ");
				} else {
					WebsiteManager.getInstance().addWebsite(web);
					EventManager.callEvent(new WebsiteLoadedEvent(web));
				}
			} else {
				logger.error("An error appear when loading " + file.getName());
			}
		}

		logger.info(WebsiteManager.getInstance().getWebsites().size() + " configs loaded !");
	}

	private boolean checkConfig(Website web) {
		UrlValidator defaultValidator = new UrlValidator();
		List<Link> crawling = new ArrayList<Link>();

		if (web.getName().isEmpty()) {
			logger.error("Invalid config name !");
			return false;
		}

		if (WebsiteManager.getInstance().existWebsiteName(web.getName())) {
			logger.error("Config name already in use !");
			return false;
		}

		logger.info("Checking config : " + web.getName());

		if (!defaultValidator.isValid(web.getUrl())) {
			logger.error("Invalid config url !");
			return false;
		}

		if (web.getRules().length == 0) {
			logger.info("This config has no rule, so it's useless");
			return false;
		}

		for (Rule rule : web.getRules()) {
			if (rule.getName().isEmpty()) {
				logger.error("| Invalid rule name !");
				return false;
			}

			logger.info("| Checking rule : " + rule.getName());

			if (!defaultValidator.isValid(rule.getRoute().replace("%{MAIN_URL}", web.getUrl()))) {
				logger.error("| Invalid route url !");
				return false;
			}

			if (rule.getActions().length == 0) {
				logger.info("| This rule has no action, so it's useless");
				return false;
			}

			if (rule.isCrawler()) {
				crawling.add(new Link(rule.getRoute(), web, rule, rule.getActions()));
				logger.info("| Crawler is activated for this rule");

				if (!rule.getDefaultParams().isEmpty()) {
					String reg = "\\(:[a-zA-Z]+\\)";
					String url = rule.getRoute();

					if (url.endsWith("/")) {
						url = url.substring(0, url.length() - 1);
					}

					String[] path = ExtractUtil.formatUrl(url, web).split(reg);

					if (path.length != rule.getDefaultParams().size()) {
						logger.error(
								"| The number of parameters in the route doesn't bot match to the size of the parameter 'defaultParams'");
						return false;
					}

					String formattedUrl = "";

					for (int i = 0; i < path.length; i++) {
						formattedUrl += path[i] + rule.getDefaultParams().get(i);
					}

					logger.info("| Queeing " + formattedUrl + " to the list of links to visit");
					CrawlerManager.getInstance().addToQueue(new Link(formattedUrl, web, rule, rule.getActions()));
				}
			} else {
				if (rule.getInitialDelay() == -1) {
					logger.error("| Invalid Initial Delay");
					return false;
				}

				if (rule.getDelay() <= 0) {
					logger.error("| Invalid Delay");
					return false;
				}

				try {
					TimeUnit.valueOf(rule.getTimeUnit());
				} catch (IllegalArgumentException e) {
					logger.error("| Invalid TimeUnit");
					return false;
				}
			}

			for (Action action : rule.getActions()) {
				if (action.getName().isEmpty()) {
					logger.error("|  Invalid action name !");
					return false;
				}

				logger.info("|  Checking action : " + action.getName());

				if (action.getSelector().isEmpty()) {
					logger.error("|  Invalid selector !");
					return false;
				}

				if (action.getDatas().length == 0) {
					logger.info("|  This action has no data to extract, so it's useless");
					return false;
				}

				for (Data data : action.getDatas()) {
					if (data.getName().isEmpty()) {
						logger.error("|   Invalid data name !");
						return false;
					}

					logger.info("|   Checking data : " + data.getName());

					if (data.getSelector().isEmpty() && data.getValue().isEmpty()) {
						logger.error("|   Please specify at least a selector or a value !");
						return false;
					}

					if (data.getFormat().isEmpty()) {
						logger.error("|   No format specified for this action, by default it's equals to string.");
						data.setFormat("string");
					} else {
						String format = data.getFormat();
						String type = "";
						String[] formatter = new String[] {};

						if (format.contains(":")) {
							String[] tmp = format.split(":");

							type = tmp[0];
							formatter = Arrays.copyOfRange(tmp, 1, tmp.length);

						} else {
							type = format;
						}

						if (!type.equalsIgnoreCase("string") && !type.equalsIgnoreCase("number")
								&& !type.equalsIgnoreCase("boolean")) {
							logger.error("|   Invalid data type, accepted types are : string, number and boolean.");
							return false;
						}

						if (formatter.length == 0) {
							logger.info("|   No formatter specified");
						}

						for (String id : formatter) {
							if (!(id.startsWith("attr[") && id.endsWith("]")) && !id.equalsIgnoreCase("trim")) {
								logger.error("|   Invalid data formatter, accepted formatter are : trim, attr.");
								return false;
							}
						}
					}
				}
			}
		}

		CrawlerManager.getInstance().addAllowedLinks(crawling);

		return true;
	}
}
