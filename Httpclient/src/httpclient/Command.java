package httpclient;

import java.io.*;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * An abstract class for an http command.
 *
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public abstract class Command {

	/*
	 * Constructor
	 */

	/**
	 * Constructs a new command.
	 * 
	 * @param host The host for this command.
	 * @param path The path for this command.
	 * @param writer The writer for this GetCommand
	 * @param reader The reader for this GetCommand
	 * @effect The given path is the new path of this command.
	 * @throws IllegalArgumentException the given reader is not valid.
	 * @throws IllegalArgumentException The given writer is not valid.
	 */
	public Command(String host, String path, OutputStream writer, InputStream reader) throws IllegalArgumentException {
		if(host == null || host == "")
			throw new IllegalArgumentException("The given host is not valid");
		if(writer == null)
			throw new IllegalArgumentException("The given writer is not valid");
		if(reader == null)
			throw new IllegalArgumentException("The given reader is not valid");
		this.host = host;
		this.reader = reader;
		this.writer = writer;
		setPath(path);
	}

	/*
	 * Variables
	 */

	/**
	 * The host for this command.
	 */
	private String host;
	
	/**
	 * The path of this command.
	 */
	private String path;

	/**
	 * The response to this command.
	 */
	private Object response;

	/**
	 * The writer to the host.
	 */
	private OutputStream writer;

	/**
	 * The reader from the host.
	 */
	private InputStream reader;
	
	/**
	 * The response info properties of this command.
	 */
	private ResponseInfo info;

	/*
	 * Methods
	 */
	
	/**
	 * Returns the host for this command.
	 * 
	 * @return the host for this command.
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Returns the path of this command.
	 * 
	 * @return The path of this command.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path of this command.
	 * 
	 * @param path The new path for this command.
	 * @post the given path is the new path of this command.
	 * @throws IllegalArgumentException The given path is not valid. path was null
	 *                                  or empty or the first character wasn't an /.
	 */
	public void setPath(String path) throws IllegalArgumentException {
		if ((path == null) || (path == "") || (path.indexOf("/") != 0))
			throw new IllegalArgumentException("The given path is not valid.");

		this.path = path;
	}

	/**
	 * Gets the response of this command.
	 * 
	 * @return response of this command.
	 */
	public Object getResponse() {
		return response;
	}

	/**
	 * Sets the response of this command.
	 * 
	 * @param response The response of this command.
	 * @post The given response is the response of this command.
	 * @throws IllegalArgumentException The given response is not valid.
	 */
	protected void setResponse(Object response) throws IllegalArgumentException {
		if (response == null)
			throw new IllegalArgumentException("The given response is not valid.");
		this.response = response;
	}

	/**
	 * Gets the writer for this command.
	 * 
	 * @return the writer for this command.
	 */
	public OutputStream getWriter() {
		return writer;
	}

	/**
	 * Gets the reader for this command.
	 * 
	 * @return the reader for this command.
	 */
	public InputStream getReader() {
		return reader;
	}
	
	/**
	 * Sets the response info
	 * @param info the new response info
	 * @throws IllegalArgumentException the response header was invalid.
	 */
	public void setResponseInfo(ResponseInfo info) throws IllegalArgumentException{
		if(info == null)
			throw new IllegalArgumentException();
		
		this.info = info;
	}
	
	/**
	 * Get the header list, a list of header line strings easily convertible to a hashmap 
	 * @return	header list
	 * @throws IOException Read failed
	 */	
	protected ArrayList<String> getHeaderList() throws IOException{
		ArrayList<String> header = new ArrayList<>();
		String line = readLine();
		while (line != "") {
			header.add(line);
			line = readLine();
		}
		System.out.println("HEADER:");
		
		for (String elem : header) {
			System.out.println(elem);
		}
		System.out.println("");
		return header;
	}
	
	/**
	 * Gets the body of the response
	 * @param contentLength the length of the body
	 * @return the body of the respone
	 * @throws IOException Read failed
	 */
	protected String getBody(int contentLength) throws IOException{
		String body = "";
		InputStream reader = getReader();
		for (int i=0; i<contentLength; i++) {
			body += (char)(reader.read());
		}
		return body;
	}
	
	/**
	 * Reads one line from the stream
	 *
	 * @return The read line.
	 * @throws IOException The read from the host failed.
	 */
	protected String readLine() throws IOException {
		InputStream reader = getReader();
		char[] buffer = new char[1000];
		String result;
		int counter = 0;
		char current;
		while (counter < 1000) {
			int currentInt = reader.read();
			if (currentInt != -1)
				current = (char) currentInt;
			else
				throw new IOException();
			// Check for end of line and make sure \r is not in buffer
			if (current != '\r') {
				if (current == '\n') {
					if (buffer[0] == 0)
						return "";
					else {
						String res = new String(buffer).trim();
						return res;
					}
				} else {
					buffer[counter] = current;
					counter += 1;
				}
			}
		}
		throw new BufferOverflowException();
	}
	
	/**
	 * Parsed the header or footer of the response to get the desired info.
	 * 
	 * @param header	The header or footer to parse
	 * @param isFooter	Indicates whether the given data is a footer or not
	 * @return			The data from the response header
	 */
	public ResponseInfo parseHeader(ArrayList<String> header, boolean isFooter) {
		int start;
		if (isFooter)
			start = 0;
		else
			start = 1;
		
		// Put header key values in hashmap
		HashMap<String, String> headerMap = new HashMap<>();
		for (int i = start; i < header.size(); i++) {
			String[] pair = header.get(i).split(":");
			pair[0] = pair[0].toLowerCase();
			for (int j = 0; j < pair.length; j++) {
				pair[j] = pair[j].trim();
			}

			headerMap.put(pair[0], pair[1]);
		}
		int code = 0;
		String message = "";
		
		// Parse first line if header
		// by getting cutting of everything before the first internal space
		// example statusline: HTTP/1.1 200 OK
		if (!isFooter) {
			String statusline = header.get(0);
			statusline = statusline.substring(statusline.indexOf(" ") + 1);
			code = Integer.parseInt(statusline.substring(0, statusline.indexOf(" ")));
			message = statusline.substring(statusline.indexOf(" ") + 1);
		}

		// Process header data
		boolean chunked = false;
		int contentLength;
		ContentType type;
		boolean connectionclosed = false;
		if (headerMap.containsKey("transfer-encoding")) {
			chunked = (headerMap.get("transfer-encoding").equals("chunked"));
			contentLength = 0;
		} else {
			if (headerMap.containsKey("content-length"))
				contentLength = Integer.parseInt(headerMap.get("content-length"));
			else if (isFooter || code != 200)
				contentLength = 0;
			else
				throw new IllegalResponseException("No Content-Length");
		}

		if (headerMap.containsKey("content-type")) {
			String contentType = headerMap.get("content-type");
			contentType = contentType.split(";")[0]; // TODO: Handle charsets?
			switch (contentType) {
			case "text/html":
				type = ContentType.HTML;
				break;
			case "text/plain":
				type = ContentType.TEXT;
				break;
			case "image/png":
				type = ContentType.IMAGEPNG;
				break;
			case "image/jpg":
				type = ContentType.IMAGEJPG;
				break;
			case "image/gif":
				type = ContentType.IMAGEGIF;
				break;
			default:
				type = ContentType.UNKNOWN;
				break;
			}
		} else {
			type = ContentType.UNKNOWN;
		}

		if (headerMap.containsKey("connection"))
			connectionclosed = (headerMap.get("connection") == "close");

		return new ResponseInfo(code, message, chunked, contentLength, connectionclosed, type);
	}

	
	/**
	 * Gets the response info
	 * @return the response info
	 */
	public ResponseInfo getResponseInfo() {
		return info;
	}

	/**
	 * Executes this command.
	 * 
	 * @post The response of this command is the new response.
	 * @throws IOException The reading of the response failed.
	 * @return boolean that indicates whether or not to close the connection.
	 */
	public abstract boolean executeCommand() throws IOException;
	
}
