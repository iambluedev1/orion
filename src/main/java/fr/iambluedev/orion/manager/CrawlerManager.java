package fr.iambluedev.orion.manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import fr.iambluedev.orion.Orion;
import fr.iambluedev.orion.object.Link;
import fr.iambluedev.orion.util.Callback;
import lombok.Getter;

@Getter
public class CrawlerManager {

	private static CrawlerManager instance;
	private final static Logger logger = Logger.getLogger(CrawlerManager.class);

	private Queue<Link> q;
	private List<Link> allowedLinks;
	private List<String> visitedLinks;

	private CrawlerManager() {
		this.init();
		this.q = new LinkedList<Link>();
		this.allowedLinks = new ArrayList<Link>();
		this.visitedLinks = new ArrayList<String>();
	}

	private void init() {
		Orion.getInstance().getDb().get(new Callback<Connection, RethinkDB>() {
			@Override
			public void call(Connection c, RethinkDB r) {
				List<String> tables = r.db(Orion.getInstance().getConfig().getDbName()).tableList().run(c);
				for (String dbName : new String[] { "visited_links" }) {
					if (!tables.contains(dbName)) {
						logger.info("Table for " + dbName + " doesn't exist, so creating it");
						r.db(Orion.getInstance().getConfig().getDbName()).tableCreate(dbName).run(c);
					}
				}
			}
		});
	}

	public static CrawlerManager getInstance() {
		if (instance == null) {
			instance = new CrawlerManager();
		}

		return instance;
	}

	public void addAllowedLink(Link link) {
		if (!this.allowedLinks.contains(link)) {
			this.allowedLinks.add(link);
		}
	}

	public void addAllowedLinks(List<Link> links) {
		for (Link link : links) {
			this.addAllowedLink(link);
		}
	}

	public void addToQueue(Link link) {
		if(!this.visitedLinks.contains(link.getUrl())) {
			Orion.getInstance().getDb().get(new Callback<Connection, RethinkDB>() {
				@Override
				public void call(Connection c, RethinkDB r) {
					Integer time = (int) ((System.currentTimeMillis() / 1000) - (TimeUnit.SECONDS.convert(link.getRule().getDelay(), TimeUnit.valueOf(link.getRule().getTimeUnit()))));
					Cursor<?> cursor = r.table("visited_links").filter((row) -> row.getField("link").eq(link.getUrl()).and(row.getField("timestamp").gt(time))).run(c);
					if (!cursor.hasNext()) {
						CrawlerManager.getInstance().q.add(link);
					}
				}
			});
			
			this.visitedLinks.add(link.getUrl());
		}
	}
}
