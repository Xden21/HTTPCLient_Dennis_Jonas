package httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class for http HEAD commands.
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class HeadCommand extends Command {

	/**
	 * Creates a new head command.
	 * 
	 * @param host 		The host for this HeadCommand
	 * @param path  	The path of this HeadCommand
	 * @param writer 	The writer for this HeadCommand
	 * @param reader 	The reader for this HeadCommand
	 * @effect 			A new Command is constructed
	 */
	public HeadCommand(String host, String path, OutputStream writer, InputStream reader)
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
		PrintWriter writer = new PrintWriter(getWriter());
		writer.print("HEAD " + getPath() + " HTTP/1.1\r\n");
		writer.print("Host: " + getHost() + "\r\n");
		writer.print("\r\n");
		writer.flush();

		// Read the header
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

		ResponseInfo parsedHeader = parseHeader(header, false);

		setResponseInfo(parsedHeader);
		return parsedHeader.getConnectionClosed();
	}



}
