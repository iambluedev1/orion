package fr.iambluedev.orion.event;

import fr.skybeastmc.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LinkCheckEvent extends Event {

	private String link;
	
}
