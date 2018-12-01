package fr.iambluedev.orion.object;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Website {

	private String name;
	
	private String url;
	
	private Rule[] rules;
	
	@Setter
	private boolean refreshing = false;
	
	@Setter
	private Integer lastRefresh;
	
	public void toggleRefresh() {
		this.refreshing = !this.refreshing;
	}
}
