package httpclient;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.swing.text.AbstractDocument.Content;

/**
 * A class defining the useful info in a response aside from the body.
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class ResponseInfo {

	/*
	 * Constructor
	 */
	
	/**
	 * Creates a new response info object.
	 * 
	 * @param statusCode	The status code in the response.
	 * @param statusMessage	The status message in the response.
	 * @param chunked		Boolean indicating if the response is in chunked format.
	 * @param contentLength The length of the body of the message.
	 * @param connectionClosed	Boolean indicating if the connection must be closed.
	 * @param contentType		The type of content in the body.
	 * @throws IllegalArgumentException	The given content length is not valid.
	 * @throws IllegalArgumentException The given status message is not valid.
	 */
	public ResponseInfo(int statusCode, String statusMessage, boolean chunked, int contentLength, boolean connectionClosed, ContentType contentType)
			throws IllegalArgumentException {
		if(contentLength < 0)
			throw new IllegalArgumentException("Not a valid length");
		if(statusMessage == null)
			throw new IllegalArgumentException("Given status message is not valid");
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
	
	/**
	 * The status code of the response
	 */
	private int statusCode;
	
	/**
	 * The status code of the message
	 */
	private String statusMessage;
	
	/**
	 * Boolean indicating if the response body is in chunked format.
	 */
	private boolean chunked;
	
	/**
	 * The length of the response body.
	 */
	private int contentLength;
	
	/**
	 * Boolean indicating if the connection must be closed.
	 */
	private boolean connectionClosed;
	
	/**
	 * The MIME type of the response body.
	 */
	private ContentType contentType;
	
	/**
	 * The collection of resource request in the response body.
	 */
	private Stack<ResourceRequest> resourceRequests;
	
	/*
	 * Methods
	 */
	
	/**
	 * Gets the status code of this response.
	 * 
	 * @return the status code.
	 */
	public int getStatusCode() {
		return statusCode;
	}
	
	/**
	 * Gets the status message of this response.
	 * 
	 * @return the status message.
	 */
	public String getStatusMessage() {
		return statusMessage;
	}
	
	/**
	 * Gets if the body is in chunked format.
	 */
	public boolean isChunked() {
		return chunked;
	}
	
	/**
	 * Gets the length of the body.
	 * 
	 * @return the length of the body in bytes.
	 */
	public int getContentLength() {
		return contentLength;
	}
	
	/**
	 * Returns whether or not the connection must be closed.
	 */
	public boolean getConnectionClosed() {
		return connectionClosed;
	}
	
	/**
	 * Sets if the connection must be closed.
	 * 
	 * @param closed boolean indicating if the connection must be closed.
	 */
	public void setConnectionClosed(boolean closed) {
		this.connectionClosed = closed;
	}
	
	/**
	 * Gets the content type of the response body.
	 * 
	 * @return The content type of the response body.
	 */
	public ContentType getContentType() {
		return contentType;
	}
	
	/**
	 * Adds a new resource request.
	 * 
	 * @param request the new resource request.
	 */
	public void registerResourceRequest(ResourceRequest request) {
		if(request == null) {
			throw new IllegalArgumentException("The given request is not valid");
		}
		
		resourceRequests.push(request);
	}
	
	/**
	 * Gets the next resource request.
	 * 
	 * @return The next resource request.
	 * @throws EmptyStackException There are no resource requests available.
	 */
	public ResourceRequest getNextResourceRequest() throws EmptyStackException {		
		return resourceRequests.pop();
	}
	
	/**
	 * Checks if there are resource requests present.
	 * 
	 * @return true if there a resource requests present. False otherwise.
	 */
	public boolean hasResourceRequests() {
		return !resourceRequests.isEmpty();
	}
	
	/**
	 * Replace the current resource stack with the given resource stack
	 * @param resourceStack
	 * @return
	 */
	
	public void replaceStack(Stack<ResourceRequest> resourceStack) {
		resourceRequests = (Stack<ResourceRequest>) resourceStack.clone();
	}
}
