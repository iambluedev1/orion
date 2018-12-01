package fr.iambluedev.orion.object;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Link {

	private String url;
	
	private Website website;
	
	private Rule rule;
	
	private Action[] actions;
	
}
