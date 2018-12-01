package fr.iambluedev.orion.object;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class Rule {

	private String name;
	
	private String route;
	
	private Action[] actions;
	
	private Integer initialDelay = 0;
	
	private Integer delay = 0;
	
	private String timeUnit = "SECONDS";
	
	private List<String> defaultParams = new ArrayList<String>();
	
	private boolean crawler = false;
}
