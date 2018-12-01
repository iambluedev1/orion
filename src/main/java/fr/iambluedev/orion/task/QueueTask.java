package fr.iambluedev.orion.task;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import fr.iambluedev.orion.Orion;
import fr.iambluedev.orion.manager.CrawlerManager;
import fr.iambluedev.orion.object.Link;
import fr.iambluedev.orion.util.Callback;
import fr.iambluedev.orion.util.ExtractUtil;

public class QueueTask extends Thread {

	private final static Logger logger = Logger.getLogger(QueueTask.class);
	private Integer activeConnection = 0;
	
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted() && Orion.getInstance().isRunning()){
			if(!CrawlerManager.getInstance().getQ().isEmpty() && activeConnection < 10) {
				Orion.getInstance().getDb().get(new Callback<Connection, RethinkDB>() {
					@Override
					public void call(Connection c, RethinkDB r) {
						activeConnection++;
						
						Link link = CrawlerManager.getInstance().getQ().poll();
						
						try {
							logger.info("|  Extracting data from " + link.getUrl() + " (" + CrawlerManager.getInstance().getQ().size() + " links in the queue)");
							Document doc = Jsoup.connect(link.getUrl()).get();
							ExtractUtil.extract(link.getWebsite(), link.getActions(), doc);
						} catch (IOException e) {
							logger.error("An error occur when requesting " + link.getUrl());
							e.printStackTrace();
						}
						
						r.table("visited_links").insert(r.hashMap("link", link.getUrl()).with("timestamp", (int) (System.currentTimeMillis() / 1000))).run(c);
					
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						activeConnection--;
					}
				});
			}
		}
	}
}
