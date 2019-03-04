package htmlclient;

import java.io.*;

/**
 * An abstract class for an html command.
 * 
 * @author Dennis Debree
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
	private String response;

	/**
	 * The writer to the host.
	 */
	private OutputStream writer;

	/**
	 * The reader from the host.
	 */
	private InputStream reader;

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
	public String getResponse() {
		return response;
	}

	/**
	 * Sets the response of this command.
	 * 
	 * @param response The response of this command.
	 * @post The given response is the response of this command.
	 * @throws IllegalArgumentException The given response is not valid.
	 */
	protected void setResponse(String response) throws IllegalArgumentException {
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
	 * Executes this command.
	 * 
	 * @post The response of this command is the new response.
	 * @throws IOException The reading of the response failed.
	 * @return boolean that indicates wether or not to close the connection.
	 */
	public abstract boolean executeCommand() throws IOException;
}