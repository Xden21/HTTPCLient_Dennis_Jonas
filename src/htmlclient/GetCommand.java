package htmlclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.HashMap;

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
	public boolean executeCommand() throws IOException {
		sendRequest();
		return processResponse();
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
	 * 
	 * @throws IOException
	 */
	private boolean processResponse() throws IOException {
		// Stub. Must be expanded for pictures, possible chuncked data. And use the text
		// length in header for efficient reading.
		// TODO: Read header: - check if content-length or chuncked data
		// - check for connection close DONE
		// - check for status code 100 (NOT)
		// - check for MIME type
		// TODO: Read body: - if MIME type is text/html
		// - if content-length: use this for reading rest of page DONE (see remarks)
		// - if chuncked data: use this for reading rest of page DONE (see remarks)
		// - parse html file to find MIME data to be downloaded
		// - save html to file
		// - if MIME type is image: download and save
		ArrayList<String> header = new ArrayList<>();
		String line = readLine(getReader());
		while (line != "") {
			header.add(line);
			line = readLine(getReader());
		}

		for (String elem : header) {
			System.out.println(elem);
		}
		System.out.println("");

		ResponseHeader parsedHeader = parseHeader(header);

		// TODO handle other types

		//CASE: HTML page
		String response = getResponse(parsedHeader);
		setResponse(response);				
		//TODO save file
		//TODO parse html to find MIME data.
		
		
		
		return parsedHeader.getConnectionClosed();
	}

	private String getResponse(ResponseHeader header) throws IOException {
		String response = "";
		if (header.isChunked()) {
			response = readChunked(header);
		} else {
			response = readFull(header);
		}
		return response;
	}
	
	private ResponseHeader parseHeader(ArrayList<String> header) {
		HashMap<String, String> headerMap = new HashMap<>();
		for (int i = 1; i < header.size(); i++) {
			String[] pair = header.get(i).split(":");
			pair[0] = pair[0].toLowerCase();
			for (int j = 0; j < pair.length; j++) {
				pair[j] = pair[j].trim();
			}

			headerMap.put(pair[0], pair[1]);
		}
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

		return new ResponseHeader(chunked, contentLength, connectionclosed, type);
	}

	//TODO optimize?
	private String readChunked(ResponseHeader info) throws IOException {
		boolean lastchunk = false;
		String body = "";
		int amount = 0;
		BufferedReader reader = new BufferedReader(new InputStreamReader(getReader()));
		while (!lastchunk) {
			try {
				String hexAmount = reader.readLine();
				amount = Integer.parseInt(hexAmount, 16); // read amount
				if (amount == 0)
					lastchunk = true;
				else {
					String chunk;
					char[] buffer = new char[amount];
					// int count = reader.read(buffer, 0, amount);
					// chunk = new String(buffer);
					// int rest = amount - count;
					for (int i = 0; i < amount; i++) {
						buffer[i] = (char) reader.read(); // TODO: More efficient solution (even though buffered)? When
															// using read with buffer, null values get read.
					}
					// while(rest != 0) {
					// buffer = new char[rest];
					// count = reader.read(buffer, 0, rest);
					// String part = new String(buffer);
					// chunk += part;
					// rest = rest - count;
					// }
					chunk = new String(buffer);
					body += chunk; // add to body
					reader.readLine(); // read the CRLF
				}
			} catch (NumberFormatException ex) {
				amount = 0;
				lastchunk = true;
			}
		}

		// Read footers
		ArrayList<String> footer = new ArrayList<>();
		String line = reader.readLine();
		while (!line.equals("")) {
			footer.add(line);
			line = reader.readLine();
		}

		for (String elem : footer) {
			System.out.println(elem);
		}
		// Handle them
		ResponseHeader parsedFooter = parseHeader(footer);
		if (parsedFooter.getConnectionClosed()) // Possible that this is in footer? Check for other possible ones that
												// matter to us.
			info.setConnectionClosed(true);

		return body;
	}

	//TODO make sure whole response will be read
	private String readFull(ResponseHeader info) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(getReader()));
		char[] buffer = new char[info.getContentLength()];
		if ((reader.read(buffer, 0, info.getContentLength())) != info.getContentLength())
			System.out.println("Not whole response read!");
		return new String(buffer);
	}

	private String readLine(InputStream reader) throws IOException {
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
}
