package httpclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;

/**
 * A class for http GET commands.
 * 
 * TODO More mime types?
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class GetCommand extends Command {

	/**
	 * Constructs a new GetCommand.
	 * 
	 * @param host   The host for this GetCommand
	 * @param path   The path of this GetCommand
	 * @param writer The writer for this GetCommand
	 * @param reader The reader for this GetCommand
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
	 * @throws IOException The reading of the response failed.
	 * @return boolean that indicates wether or not to close the connection.
	 */
<<<<<<< HEAD:Httpclient/src/httpclient/GetCommand.java

	public boolean processResponse() throws IOException {
		
		// Read the header
		ArrayList<String> header = getHeaderList();
 
=======
	private boolean processResponse() throws IOException {

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
>>>>>>> 2dab3ac82d0e6b052dc224aaf06fd3e1044354cc:Httpclient/src/httpclient/GetCommand.java

		ResponseInfo parsedHeader = parseHeader(header, false);

		setResponseInfo(parsedHeader);

		// Read the resonse
		Object response = readResponse();

		setResponse(response);

		// If html, parse for getting resources.
		if (parsedHeader.getContentType() == ContentType.HTML)
			parseHTMLPage((String) response);

		return parsedHeader.getConnectionClosed();
	}

	/*
	 * Parse functions
	 */

<<<<<<< HEAD:Httpclient/src/httpclient/GetCommand.java

=======
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
			connectionclosed = (headerMap.get("connection") == "close" || headerMap.get("connection") == "Closed");

		return new ResponseInfo(code, message, chunked, contentLength, connectionclosed, type);
	}
>>>>>>> 2dab3ac82d0e6b052dc224aaf06fd3e1044354cc:Httpclient/src/httpclient/GetCommand.java

	/**
	 * Parses a given html page, to check for MIME resources to get.
	 * 
	 * @param page The page to parse.
	 * @effect All embedded pictures where downloaded.
	 */
	private void parseHTMLPage(String page) {
		// Parse into well formed document for parsing.
		Document htmlpage = Jsoup.parse(page);
		// Search documents for image tags.
		Elements imageTags = htmlpage.getElementsByTag("img");
		ArrayList<String> imagePaths = new ArrayList<>();
		if (!imageTags.isEmpty()) {
			for (Element imageTag : imageTags) {
				String path = imageTag.attr("src");
				imagePaths.add(path);
			}

			// For each path, make resource request.
			for (String path : imagePaths) {
				String typeString = path.substring(path.lastIndexOf(".") + 1);
				ContentType type;
				switch (typeString.toLowerCase()) {
				case "jpg":
					type = ContentType.IMAGEJPG;
					break;
				case "png":
					type = ContentType.IMAGEPNG;
					break;
				case "gif":
					type = ContentType.IMAGEGIF;
					break;
				default:
					type = ContentType.UNKNOWN;
					break;
				}
				getResponseInfo().registerResourceRequest(new ResourceRequest(path, type));
			}
		}
	}

	/*
	 * Read functions
	 */

	/**
	 * Reads the response of the host.
	 * 
	 * @returnThe response of the host.
	 * @throws IOException              The read from the host failed.
	 * @throws IllegalResponseException The response was mallformed.
	 */
	private Object readResponse() throws IOException, IllegalResponseException {
		// Check content type and if the response will be chunked
		if (getResponseInfo().getContentType() == ContentType.HTML) {
			String response = "";
			if (getResponseInfo().isChunked()) {
				response = readChunkedPage();
			} else {
				response = readFullPage();
			}
			return response;
		} else if (getResponseInfo().getContentType() == ContentType.IMAGEJPG
				|| getResponseInfo().getContentType() == ContentType.IMAGEPNG
				|| getResponseInfo().getContentType() == ContentType.IMAGEGIF) {
			byte[] response;
			if (getResponseInfo().isChunked()) {
				response = readChunkedRaw();
			} else {
				response = readFullRaw();
			}
			return response;
		} else {
			throw new IllegalResponseException("Unsupported data type");
		}
	}

	/**
	 * Reads the response page thats in a chunked format.
	 * 
	 * @return The response page from the host
	 * @throws IOException The read from the host failed.
	 */
	private String readChunkedPage() throws IOException {
		boolean lastchunk = false;
		String body = "";
		int amount = 0;
		InputStream reader = getReader();
		while (!lastchunk) {
			try {
				String hexAmount = readLine();
				amount = Integer.parseInt(hexAmount, 16); // read amount
				if (amount == 0)
					lastchunk = true;
				else {
					String chunk;
					byte[] buffer = new byte[amount];
					int count = reader.read(buffer, 0, amount);

					int rest = amount - count;

					chunk = new String(buffer);
					chunk = chunk.replaceAll("\0", "");
					while (rest != 0) {
						buffer = new byte[rest];
						count = reader.read(buffer, 0, rest);
						String part = new String(buffer);
						part = part.replaceAll("\0", "");
						chunk += part;
						rest = rest - count;
					}
					body += chunk; // add to body
					readLine(); // read the CRLF
				}
			} catch (NumberFormatException ex) {
				amount = 0;
				lastchunk = true;
			}
		}

		// Read footers
		ArrayList<String> footer = new ArrayList<>();
		String line = readLine();
		while (!line.equals("")) {
			footer.add(line);
			line = readLine();
		}

		for (String elem : footer) {
			System.out.println(elem);
		}
		// Handle them
		ResponseInfo parsedFooter = parseHeader(footer, true);
		if (parsedFooter.getConnectionClosed()) // Possible that this is in footer? Check for other possible ones that
												// matter to us.
			getResponseInfo().setConnectionClosed(true);

		return body;
	}

	/**
	 * Reads the response page from the host that is formatted in one part.
	 * 
	 * @return The response page from the host
	 * @throws IOException The read from the host failed.
	 */
	private String readFullPage() throws IOException {
		InputStream reader = getReader();
		byte[] buffer = new byte[getResponseInfo().getContentLength()];
		String body = "";
		int count = reader.read(buffer, 0, getResponseInfo().getContentLength());
		int rest = getResponseInfo().getContentLength() - count;
		body += new String(buffer);
		body.replaceAll("\0", "");
		if (rest != 0) {
			buffer = new byte[rest];
			count = reader.read(buffer, 0, rest);
			body += new String(buffer);
			body.replaceAll("\0", "");
			rest = rest - count;
		}
		return body;
	}



	/**
	 * Reads the raw response thats in a chunked format.
	 * 
	 * @return The raw response from the host
	 * @throws IOException The read from the host failed.
	 */
	private byte[] readChunkedRaw() throws IOException {
		boolean lastchunk = false;
		int amount = 0;
		ArrayList<Byte> body = new ArrayList<>();
		InputStream reader = getReader();
		while (!lastchunk) {
			try {
				String hexAmount = readLine();
				amount = Integer.parseInt(hexAmount, 16); // read amount
				if (amount == 0)
					lastchunk = true;
				else {
					byte[] buffer = new byte[amount];
					int count = reader.read(buffer, 0, amount);
					int rest = amount - count;
					for (int i = 0; i < count; i++) {
						body.add(buffer[i]);
					}
					while (rest != 0) {
						count = reader.read(buffer, 0, amount);
						rest = rest - count;
						for (int i = 0; i < count; i++) {
							body.add(buffer[i]);
						}
					}

					readLine();
				}
			} catch (NumberFormatException ex) {
				amount = 0;
				lastchunk = true;
			}
		}
		// Read footers
		ArrayList<String> footer = new ArrayList<>();
		String line = readLine();
		while (!line.equals("")) {
			footer.add(line);
			line = readLine();
		}

		for (String elem : footer) {
			System.out.println(elem);
		}
		// Handle them
		ResponseInfo parsedFooter = parseHeader(footer, true);
		if (parsedFooter.getConnectionClosed()) // Possible that this is in footer? Check for other possible ones that
												// matter to us.
			getResponseInfo().setConnectionClosed(true);

		// Java array to list support is terrible. (use common lang?)
		Byte[] bodyarray;
		bodyarray = body.toArray(new Byte[body.size()]);
		byte[] result = new byte[body.size()];
		for (int i = 0; i < body.size(); i++) {
			result[i] = bodyarray[i];
		}
		return result;
	}

	/**
	 * Reads the raw response from the host that is formatted in one part.
	 * 
	 * @return The raw response from the host
	 * @throws IOException The read from the host failed.
	 */
	private byte[] readFullRaw() throws IOException {
		InputStream reader = getReader();
		ArrayList<Byte> body = new ArrayList<>();
		byte[] buffer = new byte[getResponseInfo().getContentLength()];
		int count = reader.read(buffer, 0, getResponseInfo().getContentLength());
		int rest = getResponseInfo().getContentLength() - count;
		for (int i = 0; i < count; i++) {
			body.add(buffer[i]);
		}
		if (rest != 0) {
			buffer = new byte[rest];
			count = reader.read(buffer, 0, rest);
			for (int i = 0; i < count; i++) {
				body.add(buffer[i]);
			}
			rest = rest - count;
		}
		// Java array to list support is terrible. (use common lang?)
		Byte[] bodyarray;
		bodyarray = body.toArray(new Byte[body.size()]);
		byte[] result = new byte[body.size()];
		for (int i = 0; i < body.size(); i++) {
			result[i] = bodyarray[i];
		}
		return result;
	}
}
