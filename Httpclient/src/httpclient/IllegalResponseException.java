package httpclient;

/**
 * An exception specifying that a response of the server is not valid.
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class IllegalResponseException extends RuntimeException {

	private static final long serialVersionUID = 8577326774177441887L;

	/**
	 * Creates a new illegal response exception.
	 */
	public IllegalResponseException() {		
	}

	/**
	 * Creates a new illegal response exception with given message.
	 * @param message the message for this exception.
	 */
	public IllegalResponseException(String message) {
		super(message);
	}
}
