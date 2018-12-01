package fr.iambluedev.orion.event;

import java.util.Map;

import fr.iambluedev.orion.object.Website;
import fr.skybeastmc.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DataCheckEvent extends Event {
	
	private Map<String, Object> extractedDatas;
	private Website website;
	
}
