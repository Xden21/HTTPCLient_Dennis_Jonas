package htmlclient;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * The html session class. This handles a connection to a host.
 * 
 * @author Dennis Debree
 *
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
			return true;
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
		if(!opened)
			return false;
		try {
			switch (command) {
			case "HEAD":
				httpCommand = new HeadCommand(getHost(), path, this.socket.getOutputStream(), this.socket.getInputStream());
				break;
			case "GET":
				httpCommand = new GetCommand(getHost(), path, this.socket.getOutputStream(), this.socket.getInputStream());
				break;
			case "PUT":
				break;
			case "POST":
				break;
			default:
				throw new UnsupportedOperationException("The given operation is not supported");
			}
		} catch (IOException e) {
			System.out.println("Socket error, closing connection");
			closeConnection();
			return false;
		}
		
		try {
			closeConnection = httpCommand.executeCommand();
		} catch (IOException e) {
			System.out.println("Command failed, closing connection");
			closeConnection();
			return false;
		}
		
		if(httpCommand.getResponse() != null) {
			System.out.println("RESPONSE:");
			System.out.println((String)httpCommand.getResponse());
		}
			
		if(closeConnection)
			closeConnection();
		
		return true;
	}
}
