package htmlclient;

import javax.swing.text.AbstractDocument.Content;

public class ResponseHeader {

	/*
	 * Constructor
	 */
	
	public ResponseHeader(int statusCode, String statusMessage, boolean chunked, int contentLength, boolean connectionClosed, ContentType contentType)
			throws IllegalArgumentException {
		if(contentLength < 0)
			throw new IllegalArgumentException("Not a valid length");
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.chunked = chunked;
		this.contentLength = contentLength;
		this.connectionClosed = connectionClosed;
		this.contentType = contentType;
	}

	/*
	 * Variables
	 */
	
	private int statusCode;
	
	private String statusMessage;
	
	private boolean chunked;
	
	private int contentLength;
	
	private boolean connectionClosed;
	
	private ContentType contentType;
	
	/*
	 * Methods
	 */
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public boolean isChunked() {
		return chunked;
	}
	
	public int getContentLength() {
		return contentLength;
	}
	
	public boolean getConnectionClosed() {
		return connectionClosed;
	}
	
	public void setConnectionClosed(boolean closed) {
		this.connectionClosed = closed;
	}
	
	public ContentType getContentType() {
		return contentType;
	}
}
