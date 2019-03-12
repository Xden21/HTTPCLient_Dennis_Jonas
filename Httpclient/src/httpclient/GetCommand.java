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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
		ZonedDateTime lastmodified = getFileLastModified(getPath());
		sendRequest(lastmodified);
		return processResponse();
	}

	/**
	 * Sends the GET request.
	 */
	private void sendRequest(ZonedDateTime modifiedSince) {
		PrintWriter writer = new PrintWriter(getWriter());
		writer.print("GET " + getPath() + " HTTP/1.1\r\n");
		writer.print("Host: " + getHost() + "\r\n");
		if (modifiedSince != null)
			writer.print("If-Modified-Since: " + modifiedSince.format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\r\n");
		writer.print("\r\n");
		writer.flush();
	}

	/**
	 * Processes the response.
	 * 
	 * @throws IOException The reading of the response failed.
	 * @return boolean that indicates wether or not to close the connection.
	 */

	public boolean processResponse() throws IOException {

		// Read the header
		ArrayList<String> header = getHeaderList();

		ResponseInfo parsedHeader = parseHeader(header, false);

		setResponseInfo(parsedHeader);

		if (parsedHeader.getStatusCode() != 304) {
			// Read the resonse
			Object response = readResponse();

			setResponse(response);

			// If html, parse for getting resources.
			if (parsedHeader.getContentType() == ContentType.HTML)
				parseHTMLPage((String) response);
		}
		return parsedHeader.getConnectionClosed();
	}

	/*
	 * Parse functions
	 */

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
		while (rest != 0) {
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

	/**
	 * 
	 * @param path
	 * @return
	 */
	private ZonedDateTime getFileLastModified(String path) throws IllegalArgumentException {
		if (path == null || path.indexOf("/") != 0)
			throw new IllegalArgumentException("given file path is not valid");
		if (path.equals("/")) {
			return null;
		}
		String extenstion = path.substring(path.lastIndexOf(".")+1);
		if(extenstion.equals("html"))
			return null;
		File file = new File("pages" + path);
		if (!file.exists() || !file.isFile())
			return null;

		long timeSinceEpoch = file.lastModified();
		Instant i = Instant.ofEpochMilli(timeSinceEpoch);
		return ZonedDateTime.ofInstant(i, ZoneId.of("GMT"));
	}
}
