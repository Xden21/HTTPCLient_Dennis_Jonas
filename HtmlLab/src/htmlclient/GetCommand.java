package htmlclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A class for html GET commands.
 * 
 * @author Dennis Debree
 */
public class GetCommand extends Command {

	/**
	 * Constructs a new GetCommand.
	 * 
	 * @param path   The path of this GetCommand
	 * @param writer The writer for this GetCommand
	 * @param reader The reader for this GetCommand
	 * @param host
	 * @effect A new Command is constructed
	 */
	public GetCommand(String host, String path, OutputStream writer, InputStream reader)
			throws IllegalArgumentException {
		super(host, path, writer, reader);
	}

	/**
	 * Executes this command. The response will be stored.
	 * 
	 * @effect the request has been sent.
	 * @effect the response has been processed.
	 */
	@Override
	public void executeCommand() throws IOException {
		
		sendRequest();
		processResponse();
	}
	
	/**
	 * Sends the GET request.
	 */
	private void sendRequest() {
		PrintWriter writer = new PrintWriter(getWriter());
		writer.print("GET " + getPath() + " HTTP/1.1\r\n");
		writer.print("Host: " + getHost() + "\r\n");
		writer.print("\r\n");
		writer.flush();
	}
	
	/**
	 * Processes the response.
	 * @throws IOException
	 */
	private void processResponse() throws IOException {
		// Stub. Must be expanded for pictures, possible chuncked data. And use the text
		// length in header for efficient reading.
		// TODO: Read header:	- check if content-length or chuncked data
		// 		 			 	- check for connection close
		//					 	- check for status code 100
		//						- check for MIME type
		// TODO: Read body:   	- if MIME type is text/html
		//						- if content-length: use this for reading rest of page
		//						- if chuncked data: use this for reading rest of page
		//						- parse html file to find MIME data to be downloaded
		//						- save html to file
		//						- if MIME type is image: download and save
		BufferedReader reader = new BufferedReader(new InputStreamReader(getReader()));
		String response = "";
		String line;
		while ((line = reader.readLine()) != null) {
			response += line + "\r\n";
		}

		setResponse(response);
	}

}
