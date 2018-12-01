package fr.iambluedev.orion.manager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import fr.iambluedev.orion.config.Config;

public class ConfigManager {

	private final static Logger logger = Logger.getLogger(ConfigManager.class);

	public static Config loadConfig() throws IOException {
		File file = new File("config.json");
		Gson gson = new Gson();
		if (file.exists()) {
			logger.info("Loading config file");
			return gson.fromJson(new FileReader(file), Config.class);
		} else {
			logger.info("Saving config file");
			Config config = new Config();
			config.setPort(8080);
			config.setDbHost("localhost");
			config.setDbPort(28015);
			config.setDbName("orion");

			try (Writer writer = new FileWriter(file)) {
				gson.toJson(config, writer);
			}

			return config;
		}
	}
}
