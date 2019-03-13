package httpserver;

/**
 * A class describing all important data in a request aside from the body.
 *  
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class RequestInfo {

	/**
	 * Constructs a new request info object
	 * 
	 * @param command the command of the request
	 * @param path the path of the request
	 * @param date the date the request was made
	 * @param modifiedSince the modified since value
	 * @param connectionClose indicating if the connection must be closed
	 * @param isBadRequest bool indicating if the request was mallformed
	 * @param contentType the type of the incoming body
	 * @param contentLength the length of the incoming body
	 * @throws IllegalArgumentException A given argument wasn't valid
	 */
	public RequestInfo(String command, String path, String date, String modifiedSince, boolean connectionClose,
			boolean isBadRequest, ContentType contentType, int contentLength) throws IllegalArgumentException {
		if(!isValidCommand(command))
			throw new IllegalArgumentException("Given command is not valid");
		if(path == null || path == "" || path.indexOf("/") != 0)
			throw new IllegalArgumentException("Given path is not valid");
		if(date == null)
			throw new IllegalArgumentException("Given date is not valid");
		if(modifiedSince == null)
			throw new IllegalArgumentException("Given modified since is not valid");
		if(contentType == null)
			throw new IllegalArgumentException("Given content type is not valid or supported");
		if(contentLength < 0)
			throw new IllegalArgumentException("Given content length is not valid");
		
		this.command = command;
		this.path = path;
		this.date = date;
		this.modifiedSince = modifiedSince;
		this.connectionClose = connectionClose;
		this.isBadRequest = isBadRequest;
		this.contentLength = contentLength;
	}

	/**
	 * The command of the request
	 */
	private String command;
	
	/**
	 * The path of the request
	 */
	private String path;
	
	/**
	 * The date of the request
	 */
	private String date;
	
	/**
	 * Boolean indicating whether to close the connection
	 */
	private boolean connectionClose;
	
	/**
	 * Date for checking if file has been modified since
	 */
	private String modifiedSince;
	
	/**
	 * Boolean indicating of this request is bad
	 */
	private boolean isBadRequest;
	
	/**
	 * The content type of the incoming body
	 */
	private ContentType contentType;
	
	/**
	 * The length of the incoming body
	 */
	private int contentLength;
	
	/**
	 * Checks if the command is valid
	 * 
	 * @param command the command to check
	 * @return true if the command is GET or HEAD or POST or PUT, false otherwise
	 */
	public static boolean isValidCommand(String command) {
		if(command == null)
			return false;
		if(command.equals("GET") || command.equals("HEAD") || command.equals("POST") || command.equals("PUT")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the command
	 */
	public String getCommand() {
		return command;
	}
	
	/**
	 * Returns the path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Returns the date
	 */
	public String getDate() {
		return date;
	}
	
	/**
	 * Returns the modified since value
	 */
	public String getModifiedSince() {
		return modifiedSince;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean getConnectionClose() {
		return connectionClose;
	}
	
	/**
	 * Returns whether or not this request is malformed
	 */
	public boolean IsBadRequest() {
		return isBadRequest;
	}
	
	/**
	 * Returns the content length of the incoming body
	 */
	public int GetContentLength() {
		return contentLength;
	}
	
	/**
	 * Returns the content type of the incoming body
	 */
	public ContentType GetContentType() {
		return contentType;
	}
}
