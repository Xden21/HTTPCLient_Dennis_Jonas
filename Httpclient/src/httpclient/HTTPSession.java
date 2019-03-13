package httpclient;

import java.io.*;
import java.net.*;
import java.util.Stack;

/**
 * The http session class. This handles a connection to a host.
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class HTTPSession {

	/**
	 * Creates a new http session.
	 * 
	 * @param host The host for this session
	 * @param port The port for this session
	 */
	public HTTPSession(String host, int port) {
		if (host == null || host == "")
			throw new IllegalArgumentException("Given host is not valid");
		if (port < 0 || port > 65535)
			throw new IllegalArgumentException("Given port is not valid");
		this.host = host;
		this.port = port;
		this.opened = false;
	}

	/**
	 * The socket to the host for this http session.
	 */
	private Socket socket;

	/**
	 * The host for this http session.
	 */
	private String host;

	/**
	 * The port for this connection.
	 */
	private int port;

	/**
	 * Boolean giving whether this session is opened.
	 */
	private boolean opened;

	/**
	 * Returns the host this session connects to.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port this session connects to.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Opens the socket connection to the host.
	 * 
	 * @return true of connection succeeded, false if it failed.
	 */
	public boolean openConnection() {
		try {
			this.socket = new Socket(getHost(), getPort());
			this.opened = true;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Close the socket connection to the host.
	 * 
	 * @return True if closing succeeded, false if it failed.
	 */
	public boolean closeConnection() {
		if (opened) {
			try {
				this.socket.close();
				return true;
			} catch (IOException e) {
				return false;
			}
		} else
			return true;
	}

	/**
	 * Sends the given command to the host and processes the response.
	 * 
	 * @param command The command to send
	 * @param path    The path for the command.
	 * @throws UnsupportedOperationException The given command is unknown or not
	 *                                       supported.
	 * @throws IOException                   A problem occured reading or writing to
	 *                                       the socket
	 */
	public boolean sendCommand(String command, String path) throws UnsupportedOperationException, IOException {
		Command httpCommand = null;
		boolean closeConnection = false;
		OutputStream outputStream;
		InputStream inputStream;
		if (!this.opened) {
			System.out.println("Connection not open");
			return false;
		}

		// Gets the input and outputstreams.
		try {
			outputStream = socket.getOutputStream();

			inputStream = socket.getInputStream();
		} catch (IOException e1) {
			System.out.println("Failed to get streams, closing connection");
			closeConnection();
			return false;
		}

		// Create command
		switch (command) {
		case "HEAD":
			httpCommand = new HeadCommand(getHost(), path, outputStream, inputStream);
			break;
		case "GET":
			httpCommand = new GetCommand(getHost(), path, outputStream, inputStream);
			break;
		case "PUT":
			System.out.println("Please enter the body here:");
			BufferedReader putReader = new BufferedReader(new InputStreamReader(System.in));
			String putString = putReader.readLine();
			httpCommand = new PutCommand(getHost(), path, outputStream, inputStream, putString);
			break;
		case "POST":
			System.out.println("Please enter the body here:");
			BufferedReader postReader = new BufferedReader(new InputStreamReader(System.in));
			String postString = postReader.readLine();
			httpCommand = new PostCommand(getHost(), path, outputStream, inputStream, postString);
			break;
		default:
			throw new UnsupportedOperationException("The given operation is not supported");
		}

		// Execute command
		try {
			closeConnection = httpCommand.executeCommand();
		} catch (IOException e) {
			System.out.println("Command execution failed, closing connection");
			closeConnection();
			return false;
		}

		// Add blocker
		if (command.equals("GET")) {
			ResponseInfo info = httpCommand.getResponseInfo();
			String newPage = AdBlocker.blockAdvertisements((String) httpCommand.getResponse(), info);
			httpCommand.setResponse(newPage);
		}

		if (httpCommand.getResponse() != null && (httpCommand.getResponseInfo().getContentType() == ContentType.HTML)
				|| (httpCommand.getResponseInfo().getContentType() == ContentType.TEXT)) {
			System.out.println("RESPONSE:");
			System.out.println((String) httpCommand.getResponse());
		}

		// Save response to disk
		if (httpCommand.getResponseInfo().getContentType() == ContentType.HTML) {
			if (httpCommand.getResponseInfo().getStatusCode() == 200) {
				try {
					savePage((String) httpCommand.getResponse(), httpCommand.getResponseInfo(), path);
				} catch (FileNotFoundException e) {
					System.out.println("Page save failed");
				}
			}
		} else if (httpCommand.getResponseInfo().getContentType() != ContentType.UNKNOWN) {
			if (httpCommand.getResponseInfo().getStatusCode() == 200) {
				try {
					saveResource((byte[]) httpCommand.getResponse(), httpCommand.getPath());
				} catch (Exception ex) {
					System.out.println("Resource save failed");
				}
			}
		}

		// If host requested connection close, do so.
		if (closeConnection) {
			closeConnection();
			return true;
		}

		// Check for resource requests
		while (httpCommand.getResponseInfo().hasResourceRequests()) {
			// For each resource request, perform a get command.
			ResourceRequest request = httpCommand.getResponseInfo().getNextResourceRequest();
			if (request.getPath().contains("http")) {
				try {
					URI contentHost = new URI(request.getPath());
					HTTPSession contentSession = new HTTPSession(contentHost.getHost(), 80);
					if (!contentSession.openConnection()) {
						System.out.println("Connection failed");
					} else {
						if (!contentSession.sendCommand("GET", contentHost.getPath()))
							System.out.println("Resource command failed.");
						if (!contentSession.closeConnection()) {
							System.out.println("Connection closed failed");
						}

					}
				} catch (URISyntaxException e) {
					System.out.println("Resource command failed, uri not known");
				}

				if (closeConnection) {
					closeConnection();
					return false;
				}
			} else {
				try {
					GetCommand requestCommand = new GetCommand(getHost(), "/" + request.getPath(), outputStream,
							inputStream);
					closeConnection = requestCommand.executeCommand();
					if (requestCommand.getResponseInfo().getStatusCode() == 200) {
						try {
							// Save the resource to disk.
							saveResource((byte[]) requestCommand.getResponse(), requestCommand.getPath());
						} catch (Exception ex) {
							System.out.println("Resource save failed");
						}
					}
					if (closeConnection) {
						closeConnection();
						return true;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("Resource Command failed, closing connection");

				}
			}

		}

		System.out.println("Request finished");
		return true;

	}

	/**
	 * Saves the given html page to disk.
	 * 
	 * @param page     The html page to save.
	 * @param response The response info of the command that fetched the page.
	 * @throws FileNotFoundException The file couldn't be saved.
	 */
	private void savePage(String page, ResponseInfo response, String path) throws FileNotFoundException {
		File dir = new File("pages/");
		if (!dir.exists())
			dir.mkdirs();
		PrintWriter filewriter;
		if (path == "/") {
			filewriter = new PrintWriter("pages/" + getHost() + "." + response.getContentType().getExtension());
		} else {
			filewriter = new PrintWriter("pages/" + path);
		}

		filewriter.print(page);
		filewriter.flush();
		filewriter.close();
	}

	/**
	 * Saves the given dataset to disk.
	 * 
	 * @param resource The dataset to save.
	 * @param path     The path to save to, including file name and extension
	 * @throws IOException           The file couldn't be written to.
	 * @throws FileNotFoundException The file couldn't be opened or created.
	 */
	private void saveResource(byte[] resource, String path) throws IOException, FileNotFoundException {
		String dirpath = "pages/" + path.substring(1, path.lastIndexOf("/") + 1);
		File dir = new File(dirpath);
		if (!dir.exists())
			dir.mkdirs();

		FileOutputStream filewriter = new FileOutputStream("pages" + path);
		filewriter.write(resource);
		filewriter.flush();
		filewriter.close();
	}

}
