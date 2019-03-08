package htmlclient;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * The html session class. This handles a connection to a host.
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class HTTPSession {

	/**
	 * 
	 * @param host
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
	 * 
	 * @return
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
	 * 
	 * @return
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
	 * 
	 * @param command
	 * @param path
	 */
	public boolean sendCommand(String command, String path) throws UnsupportedOperationException {
		Command httpCommand = null;
		boolean closeConnection = false;
		OutputStream outputStream;
		InputStream inputStream;
		if(!this.opened) {
			System.out.println("Connection not open");
			return false;
		}
		
		try {
			outputStream = socket.getOutputStream();

			inputStream = socket.getInputStream();
		} catch (IOException e1) {
			System.out.println("Failed to get streams, closing connection");
			closeConnection();
			return false;
		}
			
		switch (command) {
		case "HEAD":
			httpCommand = new HeadCommand(getHost(), path, outputStream, inputStream);
			break;
		case "GET":
			httpCommand = new GetCommand(getHost(), path, outputStream, inputStream);
			break;
		case "PUT":
			break;
		case "POST":
			break;
		default:
			throw new UnsupportedOperationException("The given operation is not supported");
		}
		
		try {
			closeConnection = httpCommand.executeCommand();
		} catch (IOException e) {
			System.out.println("Command execution failed, closing connection");
			closeConnection();
			return false;
		}
		
		if(httpCommand.getResponse() != null) {
			System.out.println("RESPONSE:");
			System.out.println((String)httpCommand.getResponse());
		}
		
		if(httpCommand.getResponseInfo().getContentType() == ContentType.HTML) {
			if(httpCommand.getResponseInfo().getStatusCode() == 200) {
				try {
					savePage((String) httpCommand.getResponse(), httpCommand.getResponseInfo());
				} catch (FileNotFoundException e) {
					System.out.println("Page save failed");
				}
			}
		} else if (httpCommand.getResponseInfo().getContentType() != ContentType.UNKNOWN) {
			if(httpCommand.getResponseInfo().getStatusCode() == 200) {
				try {
					saveResource((byte[]) httpCommand.getResponse(), httpCommand.getPath());
				} catch (Exception ex) {
					System.out.println("Resource save failed");
				}
			}
		}
		

		if(closeConnection) {
			closeConnection();
			return true;
		}
		
		// TODO Implement add blocker
		
		// Check for resource requests
		while(httpCommand.getResponseInfo().hasResourceRequests()) {
			ResourceRequest request = httpCommand.getResponseInfo().getNextResourceRequest();
			try {
				GetCommand requestCommand = new GetCommand(getHost(),"/" +  request.getPath(), outputStream, inputStream);
				closeConnection = requestCommand.executeCommand();
				if(requestCommand.getResponseInfo().getStatusCode() == 200) {
					try {
						saveResource((byte[]) requestCommand.getResponse(), requestCommand.getPath());
					} catch (Exception ex) {
						System.out.println("Resource save failed");
					}
				}
				if(closeConnection) {
					closeConnection();
					return true;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Resource Command failed, closing connection");
				closeConnection();
				return false;
			}
		}
		
		System.out.println("Request finished");
		return true;
	}
	
	private void savePage(String page, ResponseInfo response) throws FileNotFoundException {
		File dir = new File("pages/");
		if (!dir.exists())
			dir.mkdirs();

		PrintWriter filewriter = new PrintWriter(
				"pages/" + getHost() + "." + response.getContentType().getExtension());
		filewriter.print(page);
		filewriter.flush();
		filewriter.close();
	}
	
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
