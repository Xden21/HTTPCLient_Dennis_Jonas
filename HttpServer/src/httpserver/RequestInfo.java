package httpserver;

public class RequestInfo {

	public RequestInfo(String command, String path, String date, String modifiedSince, boolean connectionClose, boolean isBadRequest, ContentType contentType) {
		if(!isValidCommand(command))
			throw new IllegalArgumentException("Given command is not valid");
		if(path == null || path == "" || path.indexOf("/") != 0)
			throw new IllegalArgumentException("Given path is not valid");
		if(date == null)
			throw new IllegalArgumentException("Given date is not valid");
		if(modifiedSince == null)
			throw new IllegalArgumentException("Given modified since is not valid");
		if(contentType == null || contentType == ContentType.UNKNOWN)
			throw new IllegalArgumentException("Given content type is not valid or supported");
		if(contentLength < 0)
			throw new IllegalArgumentException("Given content length is not valid");
		
		this.command = command;
		this.path = path;
		this.date = date;
		this.modifiedSince = modifiedSince;
		this.connectionClose = connectionClose;
		this.isBadRequest = isBadRequest;
	}

	
	private String command;
	
	private String path;
	
	private String date;
	
	private boolean connectionClose;
	
	private String modifiedSince;
	
	private boolean isBadRequest;
	
	private ContentType contentType;
	
	private int contentLength;
	
	/**
	 * 
	 * @param command
	 * @return
	 */
	public static boolean isValidCommand(String command) {
		if(command == null)
			return false;
		if(command == "GET" || command == "HEAD" || command == "POST" || command == "PUT") {
			return true;
		}
		return false;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getModifiedSince() {
		return modifiedSince;
	}
	
	public boolean getConnectionClose() {
		return connectionClose;
	}
	
	public boolean IsBadRequest() {
		return isBadRequest;
	}
	
	public int GetContentLength() {
		return contentLength;
	}
}
