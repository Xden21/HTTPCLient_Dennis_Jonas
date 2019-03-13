package httpserver;

/**
 * Enum of all the possible response codes
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public enum ResponseCode {
	SUCCESS(200, "OK"), CREATED(201, "Created"), NOT_FOUND(404, "Not Found"), BAD_REQUEST(400, "Bad Request"),
	SERVER_ERROR(500, "Server Error"), NOT_MODIFIED(304, "Not Modified");
	
	/**
	 * Creates a response code
	 * @param code the numerical code
	 * @param message the message with the code
	 */
	private ResponseCode(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	/**
	 * The numerical code of the response code
	 */
	private int code;
	
	/**
	 * The message of the response code
	 */
	private String message;
	
	/**
	 * Returns the numerical code of the response code
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 * Returns the message of the response code
	 */
	public String getMessage() {
		return message;
	}
}
