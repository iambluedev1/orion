package fr.iambluedev.orion.manager;

import java.util.ArrayList;
import java.util.List;

import fr.iambluedev.orion.object.Action;
import fr.iambluedev.orion.object.Rule;
import fr.iambluedev.orion.object.Website;
import lombok.Getter;

@Getter
public class WebsiteManager {

	private List<Website> websites;

	private static WebsiteManager instance;
	
	private WebsiteManager() {
		this.websites = new ArrayList<Website>();
	}

	public static WebsiteManager getInstance() {
		if (instance == null) {
			instance = new WebsiteManager();
		}

		return instance;
	}
	
	public void addWebsite(Website web) {
		this.websites.add(web);
	}

	public boolean existWebsite(Website web) {
		return this.websites.contains(web);
	}

	public boolean existWebsiteName(String name) {
		for (Website web : this.websites) {
			if (web.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public Integer[] stats() {
		Integer countRules = 0;
		Integer countActions = 0;
		Integer dataToExtract = 0;

		for (Website web : this.websites) {
			countRules += web.getRules().length;

			for (Rule rule : web.getRules()) {
				countActions += rule.getActions().length;
				
				for (Action action : rule.getActions()) {
					dataToExtract += action.getDatas().length;
				}
			}
		}

		return new Integer[] { this.websites.size(), countRules, countActions, dataToExtract };
	}

}
