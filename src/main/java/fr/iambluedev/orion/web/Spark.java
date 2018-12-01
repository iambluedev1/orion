package fr.iambluedev.orion.web;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.notFound;
import static spark.Spark.options;
import static spark.Spark.port;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.iambluedev.orion.manager.CrawlerManager;
import fr.iambluedev.orion.manager.WebsiteManager;
import fr.iambluedev.orion.object.Link;
import fr.iambluedev.orion.web.response.JsonResponse;

public class Spark {

	private final static Logger logger = Logger.getLogger(Spark.class);

	public void setup(Integer port) {
		logger.info("Starting web server");
		
		port(port);

		options("/*", (request, response) -> {
			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if (accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}

			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if (accessControlRequestMethod != null) {
				response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}
			return "OK";
		});

		before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

		get("/", (req, res) -> new JsonResponse(res)
				.add("name", "Orion - Webscraper")
				.add("author", "Iambluedev <iambluedev@gmx.fr>")
				.add("version", "1.0.0")
			.get());
		
		get("/stats", (req, res) -> {
			Integer[] datas = WebsiteManager.getInstance().stats();
			return new JsonResponse(res)
					.add("website", datas[0])
					.add("rules", datas[1])
					.add("actions", datas[2])
					.add("datasToExtract", datas[3])
				.get();
		});
		
		get("/websites", (req, res) -> new JsonResponse(res)
				.add("websites", WebsiteManager.getInstance().getWebsites())
			.get());
		
		get("/queue", (req, res) -> {
			List<String> links = new ArrayList<String>();
			
			for(Link link : CrawlerManager.getInstance().getQ()) {
				links.add(link.getUrl());
			}
			
			return new JsonResponse(res)
				.add("queue", links)
			.get();
		});
		
		notFound((req, res) -> new JsonResponse(res)
				.add("error", "404 - Not Found")
				.get()
		);
		
		logger.info("Web Server now listening on 127.0.0.1:" + port);
	}
}
