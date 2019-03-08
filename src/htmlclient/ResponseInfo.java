package htmlclient;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.swing.text.AbstractDocument.Content;

/**
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class ResponseInfo {

	/*
	 * Constructor
	 */
	
	public ResponseInfo(int statusCode, String statusMessage, boolean chunked, int contentLength, boolean connectionClosed, ContentType contentType)
			throws IllegalArgumentException {
		if(contentLength < 0)
			throw new IllegalArgumentException("Not a valid length");
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.chunked = chunked;
		this.contentLength = contentLength;
		this.connectionClosed = connectionClosed;
		this.contentType = contentType;
		this.resourceRequests = new Stack<ResourceRequest>();
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
	
	private Stack<ResourceRequest> resourceRequests;
	
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
	
	public void registerResourceRequest(ResourceRequest request) {
		if(request == null) {
			throw new IllegalArgumentException("The given request is not valid");
		}
		
		resourceRequests.push(request);
	}
	
	public ResourceRequest getNextResourceRequest() throws EmptyStackException {		
		return resourceRequests.pop();
	}
	
	public boolean hasResourceRequests() {
		return !resourceRequests.isEmpty();
	}
}
