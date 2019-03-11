package httpclient;

import java.io.*;
import java.util.ArrayList;

/**
 * A class for the HTML Put command implementation
 *
 * 
 */

public class PutCommand extends Command {

	/**
	 * Constructs a new PutCommand.
	 *
	 * @param host 		The host for this PutCommand
	 * @param path  	The path of this PutCommand
	 * @param writer 	The writer for this PutCommand
	 * @param reader 	The reader for this PutCommand
	 * @param input		The input for this PutCommand
	 * @effect 			A new Command is constructed
	 */
	public PutCommand(String host, String path, OutputStream writer, InputStream reader, String input) 
			throws IllegalArgumentException {
		super(host, path, writer, reader);
		this.input = input;
	}
	
	/**
	 * The input that was given along with this put command
	 */
	private String input;

	/**
	 * Executes this command.
	 * 
	 * @post The response of this command is the new response.
	 * @throws IOException The reading of the response failed.
	 * @return boolean that indicates whether or not to close the connection.
	 */
	@Override
	public boolean executeCommand() throws IOException {
		sendRequest();
		// Read the header
		ResponseInfo info = parseHeader(getHeaderList(), false);
		setResponseInfo(info);
		System.out.print("BODY:");
		System.out.print(getBody(info.getContentLength()));
		return info.getConnectionClosed();
	}

	/**
	 * Sends the PUT request.
	 */
	private void sendRequest() {
		PrintWriter writer = new PrintWriter(getWriter());
		writer.print("PUT " + getPath() + " HTTP/1.1\r\n");
		writer.print("Host: " + getHost() + "\r\n");
		writer.print("Content-Length: " + getContentLength()+ "\r\n");
		writer.print("Content-Type: text/plain\r\n");
		writer.print("\r\n");
		writer.print(input + "\r\n");
		writer.flush();
	}
	
	/**
	 * Get the content length
	 */
	public String getContentLength() {
		return Integer.toString(input.length());
	}
	
	
	
	
}