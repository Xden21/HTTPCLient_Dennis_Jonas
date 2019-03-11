package httpclient;

/**
 * A class for the HTML POST command implementation
 *
 * 
 */

public class PostCommand extends Command {

	/**
	 * Constructs a new PostCommand.
	 *
	 * @param host 		The host for this PostCommand
	 * @param path  	The path of this PostCommand
	 * @param writer 	The writer for this PostCommand
	 * @param reader 	The reader for this PostCommand
	 * @effect 			A new Command is constructed
	 */
	public PostCommand(String host, String path, OutputStream writer, InputStream reader)
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
	 * Sends the Post request.
	 */
	private void sendRequest() {
		PrintWriter writer = new PrintWriter(getWriter());
		writer.print("Post " + getPath() + " HTTP/1.1\r\n");
		writer.print("Host: " + getHost() + "\r\n");
		writer.print("\r\n");
		writer.flush();
	}
}