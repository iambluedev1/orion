package fr.iambluedev.orion.util;

import lombok.Getter;

@Getter
public enum RegexUtil {

	ANY("[^/]+", "(:any)"),
	NUM("-?[0-9]+", "(:num)"),
	ALL(".*", "(:all)"),
	LET("[a-zA-Z]+", "(:let)");
	
	private String regex;
	private String identifier;
	
	private RegexUtil(String regex, String identifier){
		this.identifier = identifier;
		this.regex = regex;
	}

	public static RegexUtil fromString(String type) {
		if (type != null) {
			for (RegexUtil types : RegexUtil.values()) {
				if (type.equalsIgnoreCase(types.getIdentifier())) {
					return types;
				}
			}
	    }
	    return null;
	}
}
