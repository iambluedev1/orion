package fr.skybeastmc.events;

/**
 * This exception is logged when a listener throw an exception.
 */
public class EventException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new EventException.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public EventException(String message, Throwable cause) {
		super(message, cause);
	}
}
