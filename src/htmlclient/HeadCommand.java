package htmlclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.HashMap;

public class HeadCommand extends Command {

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

	/**
	 * Reads one line from the stream
	 *
	 * @return The read line.
	 * @throws IOException The read from the host failed.
	 */
	private String readLine() throws IOException {
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
	 * @param header   The header or footer to parse
	 * @param isFooter Indicates wether the given data is a footer or not
	 * @return The data from the response header
	 */
	private ResponseInfo parseHeader(ArrayList<String> header, boolean isFooter) {
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
			else if (isFooter)
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

}
