package fr.iambluedev.orion.event;

import fr.iambluedev.orion.object.Website;
import fr.skybeastmc.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WebsiteLoadedEvent extends Event {

	private Website website;
	
}
