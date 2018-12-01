package fr.iambluedev.orion.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import fr.iambluedev.orion.object.Rule;
import fr.iambluedev.orion.object.Website;
import fr.iambluedev.orion.util.ExtractUtil;
import lombok.Getter;

@Getter
public class RefreshTask extends TimerTask {

	private final static Logger logger = Logger.getLogger(RefreshTask.class);
	private Website website;
	private Rule rule;

	private List<String> links;

	public RefreshTask(Website web, Rule rule) {
		this.website = web;
		this.rule = rule;

		this.links = new ArrayList<String>();
	}

	@Override
	public void run() {
		this.website.toggleRefresh();
		this.website.setLastRefresh((int) (System.currentTimeMillis() / 1000));
		logger.info("Starting Refresh Task for " + this.website.getName() + ":" + this.rule.getName());

		try {
			Document doc = Jsoup.connect(ExtractUtil.formatUrl(this.rule.getRoute(), this.website)).get();
			ExtractUtil.extract(this.website, this.rule.getActions(), doc);
		} catch (IOException e) {
			logger.error("An error occur when requesting " + ExtractUtil.formatUrl(this.rule.getRoute(), this.website));
			e.printStackTrace();
		}

		this.website.toggleRefresh();
	}

	
}
