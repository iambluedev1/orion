package fr.iambluedev.orion.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {

	private Integer port;
	
	private String dbHost;
	
	private String dbName;
	
	private Integer dbPort;

}
