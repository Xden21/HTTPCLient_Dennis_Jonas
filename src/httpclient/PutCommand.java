package httpclient;

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
	 * @effect 			A new Command is constructed
	 */
	public PutCommand(String host, String path, OutputStream writer, InputStream reader) 
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
	public boolean executeCommand() throws IOException {
		sendRequest();
		return processResponse();
	}

	/**
	 * Sends the PUT request.
	 */
	private void sendRequest() {
		PrintWriter writer = new PrintWriter(getWriter());
		writer.print("PUT " + getPath() + " HTTP/1.1\r\n");
		writer.print("Host: " + getHost() + "\r\n");
		writer.print("\r\n");
		writer.flush();
	}
}