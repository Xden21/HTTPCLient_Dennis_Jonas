package htmlclient;

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
	 * @throws IOException The reading of the response failed.
	 * @return boolean that indicates wether or not to close the connection.
	 */
	private boolean processResponse() throws IOException {
		// Stub. Must be expanded for pictures, possible chuncked data. And use the text
		// length in header for efficient reading.
		// TODO: Read header: - check if content-length or chuncked data DONE
		// - check for connection close DONE
		// - check for status code 100 (NOT)
		// - check for MIME type DONE
		// TODO: Read body: - if MIME type is text/html
		// - if content-length: use this for reading rest of page DONE (see remarks)
		// - if chuncked data: use this for reading rest of page DONE (see remarks)
		// - parse html file to find MIME data to be downloaded DONE (images)
		// - save html to file DONE
		// - if MIME type is image: download and save DONE
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

		ResponseHeader parsedHeader = parseHeader(header, false);

		setHeader(parsedHeader);
		
		Object response = readResponse();

		// CASE: HTML page
		if (parsedHeader.getContentType() == ContentType.HTML) {
			setResponse(response);

			if(parsedHeader.getStatusCode() == 200) {
				// save file
				File dir = new File("pages/");
				if (!dir.exists())
					dir.mkdirs();
	
				PrintWriter filewriter = new PrintWriter("pages/" + getHost() + "." + parsedHeader.getContentType().getExtension());
				filewriter.print(response);
				filewriter.flush();
				filewriter.close();
	
				// TODO parse html to find MIME data.
				parseHTMLPage((String)response);
			}
		} else if(parsedHeader.getContentType() == ContentType.IMAGEJPG || parsedHeader.getContentType() == ContentType.IMAGEPNG
				|| parsedHeader.getContentType() == ContentType.IMAGEGIF) {
			setResponse(response);
			if(parsedHeader.getStatusCode() == 200) {
				String dirpath = "pages/" + getPath().substring(1, getPath().lastIndexOf("/")+1);
				File dir = new File(dirpath);
				if(!dir.exists())
					dir.mkdirs();
				
				FileOutputStream filewriter = new FileOutputStream("pages" + getPath());
				filewriter.write((byte[]) response);
				filewriter.flush();
				filewriter.close();
			}
		}

		return parsedHeader.getConnectionClosed();
	}

	/*
	 * Parse functions
	 */

	private ResponseHeader parseHeader(ArrayList<String> header, boolean isFooter) {
		int start;
		if(isFooter)
			start = 0;
		else
			start = 1;
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
		
		if(!isFooter)
		{
			String statusline = header.get(0);
			statusline = statusline.substring(statusline.indexOf(" ")+1);
			code = Integer.parseInt(statusline.substring(0, statusline.indexOf(" ")));
			message = statusline.substring(statusline.indexOf(" ") + 1);
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

		return new ResponseHeader(code, message, chunked, contentLength, connectionclosed, type);
	}

	private void parseHTMLPage(String page) {
		Document htmlpage = Jsoup.parse(page); // Parse into well formed document for parsing.
		Elements imageTags = htmlpage.getElementsByTag("img"); // Search documents for image tags.
		ArrayList<String> imagePaths = new ArrayList<>();
		if(!imageTags.isEmpty())
		{
			for(Element imageTag : imageTags) {
				String path = imageTag.attr("src");
				if(!path.contains("http")) {
					imagePaths.add(path);
				}
			}
			
			// For each path, do get request
			for(String path : imagePaths) {
				GetCommand imageGet = new GetCommand(getHost(), "/" + path, getWriter(), getReader());
				try {
					imageGet.executeCommand();
				} catch (IOException e) {
					System.out.println("Failed to get image");
				}
			}
		}
	}

	/*
	 * Read functions
	 */

	private Object readResponse() throws IOException, IllegalResponseException {
		if (getHeader().getContentType() == ContentType.HTML) {
			String response = "";
			if (getHeader().isChunked()) {
				response = readChunkedPage();
			} else {
				response = readFullPage();
			}
			return response;
		} else if (getHeader().getContentType() == ContentType.IMAGEJPG || getHeader().getContentType() == ContentType.IMAGEPNG
				|| getHeader().getContentType() == ContentType.IMAGEGIF) {
			byte[] response;
			if (getHeader().isChunked()) {
				response = readChunkedRaw();
			} else {
				response = readFullRaw();
			}
			return response;		
		} else {
			throw new IllegalResponseException("Unsupported data type");
		}
	}

	// TODO optimize?
	private String readChunkedPage() throws IOException {
		boolean lastchunk = false;
		String body = "";
		int amount = 0;
		InputStream reader = getReader();
		while (!lastchunk) {
			try {
				String hexAmount = readLine(reader);
				amount = Integer.parseInt(hexAmount, 16); // read amount
				if (amount == 0)
					lastchunk = true;
				else {
					String chunk;
					byte[] buffer = new byte[amount];
					// int count = reader.read(buffer, 0, amount);
					// chunk = new String(buffer);
					// int rest = amount - count;
					for (int i = 0; i < amount; i++) {
						buffer[i] = (byte) reader.read(); // TODO: More efficient solution (even though buffered)? When
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
					readLine(reader); // read the CRLF
				}
			} catch (NumberFormatException ex) {
				amount = 0;
				lastchunk = true;
			}
		}

		// Read footers
		ArrayList<String> footer = new ArrayList<>();
		String line = readLine(reader);
		while (!line.equals("")) {
			footer.add(line);
			line = readLine(reader);
		}

		for (String elem : footer) {
			System.out.println(elem);
		}
		// Handle them
		ResponseHeader parsedFooter = parseHeader(footer, true);
		if (parsedFooter.getConnectionClosed()) // Possible that this is in footer? Check for other possible ones that
												// matter to us.
			getHeader().setConnectionClosed(true);

		return body;
	}

	// TODO make sure whole response will be read
	private String readFullPage() throws IOException {
		InputStream reader = getReader();
		byte[] buffer = new byte[getHeader().getContentLength()];
		String body = "";
		int count = reader.read(buffer, 0, getHeader().getContentLength());
		int rest = getHeader().getContentLength() - count;
		body += new String(buffer);
		if(rest != 0) {
			buffer = new byte[rest];
			count = reader.read(buffer, 0, rest);
			body += new String(buffer);
			rest = rest - count;
		}
		return body;
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

	private byte[] readChunkedRaw() throws IOException {
		boolean lastchunk = false;
		int amount = 0;
		ArrayList<Byte> body = new ArrayList<>();
		InputStream reader = getReader();
		while (!lastchunk) {
			try {
				String hexAmount = readLine(reader);
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

					readLine(reader);
				}
			} catch (NumberFormatException ex) {
				amount = 0;
				lastchunk = true;
			}
		}
		// Read footers
		ArrayList<String> footer = new ArrayList<>();
		String line = readLine(reader);
		while (!line.equals("")) {
			footer.add(line);
			line = readLine(reader);
		}

		for (String elem : footer) {
			System.out.println(elem);
		}
		// Handle them
		ResponseHeader parsedFooter = parseHeader(footer, true);
		if (parsedFooter.getConnectionClosed()) // Possible that this is in footer? Check for other possible ones that
												// matter to us.
			getHeader().setConnectionClosed(true);

		// Java array to list support is terrible. (use common lang?)
		Byte[] bodyarray;
		bodyarray = body.toArray(new Byte[body.size()]);
		byte[] result = new byte[body.size()];
		for(int i = 0; i < body.size(); i++) {
			result[i] = bodyarray[i];
		}
		return result;
	}

	private byte[] readFullRaw() throws IOException {
		InputStream reader = getReader();
		byte[] buffer = new byte[getHeader().getContentLength()];
		if ((reader.read(buffer, 0, getHeader().getContentLength())) != getHeader().getContentLength()) {
			System.out.println("Not whole response read!");
		}
		return buffer;
	}
}
