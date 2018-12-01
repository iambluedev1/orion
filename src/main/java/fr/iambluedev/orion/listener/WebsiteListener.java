package fr.iambluedev.orion.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import fr.iambluedev.orion.Orion;
import fr.iambluedev.orion.event.LinkCheckEvent;
import fr.iambluedev.orion.event.WebsiteLoadedEvent;
import fr.iambluedev.orion.manager.CrawlerManager;
import fr.iambluedev.orion.manager.WebsiteManager;
import fr.iambluedev.orion.object.Action;
import fr.iambluedev.orion.object.Link;
import fr.iambluedev.orion.object.Rule;
import fr.iambluedev.orion.object.Website;
import fr.iambluedev.orion.task.RefreshTask;
import fr.iambluedev.orion.util.Callback;
import fr.iambluedev.orion.util.ExtractUtil;
import fr.iambluedev.orion.util.RegexUtil;
import fr.skybeastmc.events.EventHandler;
import fr.skybeastmc.events.Listener;

public class WebsiteListener implements Listener {

	private final static Logger logger = Logger.getLogger(WebsiteManager.class);

	@EventHandler
	public void loaded(WebsiteLoadedEvent event) {
		Website web = event.getWebsite();
		Orion.getInstance().getDb().get(new Callback<Connection, RethinkDB>() {
			@Override
			public void call(Connection c, RethinkDB r) {
				List<String> tables = r.db(Orion.getInstance().getConfig().getDbName()).tableList().run(c);

				for (Rule rule : web.getRules()) {
					for (Action action : rule.getActions()) {
						String dbName = web.getName().toLowerCase() + "_" + action.getName().toLowerCase();
						if (!tables.contains(dbName)) {
							logger.info("Table for " + dbName + " doesn't exist, so creating it");
							r.db(Orion.getInstance().getConfig().getDbName()).tableCreate(dbName).run(c);
						}
					}

					if (!rule.isCrawler()) {
						logger.info("Init Tasks for " + web.getName());
						Orion.getInstance().getScheduler().scheduleAtFixedRate(new RefreshTask(web, rule), rule.getInitialDelay(), rule.getDelay(), TimeUnit.valueOf(rule.getTimeUnit()));
					}
				}
			}
		});
	}

	@EventHandler
	public void checkLink(LinkCheckEvent event) {
		for(Link link : CrawlerManager.getInstance().getAllowedLinks()) {
			String url = ExtractUtil.formatUrl(link.getUrl(), link.getWebsite());
			
			if(url.equals(event.getLink())) {
				CrawlerManager.getInstance().addToQueue(new Link(event.getLink(), link.getWebsite(), link.getRule(), link.getActions()));
				break;
			}
			
			if(url.contains(":")) {
				String tmp = "";
        		tmp = url.substring(1);
        		tmp.replaceAll("/", "\\/");
        		for(RegexUtil pattern : RegexUtil.values()){
        			tmp = tmp.replaceAll(pattern.getIdentifier(), pattern.getRegex());
        		}
        		tmp = tmp.replaceAll("\\(/", "\\(?:/").replaceAll("\\)", "\\)?");
        		
        		Matcher matcher = Pattern.compile(tmp, Pattern.CASE_INSENSITIVE).matcher(event.getLink().substring(1));
            	List<String> uriArgs = new ArrayList<String>();
            	
            	while(matcher.find()){
            		for(Integer i = 1;i <= matcher.groupCount(); i++){
            			uriArgs.add(matcher.group(i));
            		}
            	}
            	
            	if(!uriArgs.isEmpty()){
            		CrawlerManager.getInstance().addToQueue(new Link(event.getLink(), link.getWebsite(), link.getRule(), link.getActions()));
            		break;
            	}
			}
		}
	}

}
