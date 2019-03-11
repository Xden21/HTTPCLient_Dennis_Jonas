package httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpServerSession extends Thread {

	/**
	 * 
	 */
	Socket socket;

	private boolean terminate;

	/**
	 * 
	 * @param socket
	 */
	public HttpServerSession(Socket socket) throws IllegalArgumentException {
		if (socket == null) {
			throw new IllegalArgumentException("Given socket is not valid");
		}
		this.terminate = false;
	}

	/**
	 * 
	 */
	@Override
	public void run() {
		// Read until header found.
		// Parse header
		// case: GET => send requested uri
		// case: HEAD => send head of requested uri
		// case: POST read body and append to file or create & send response header
		// case: PUT read body and save to new file & send response header
		InputStream input;
		OutputStream output;
		try {
			input = this.socket.getInputStream();
			output = this.socket.getOutputStream();
		} catch (IOException e) {
			System.out.println("Failed to get streams");
			return;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		ArrayList<String> headerLines = new ArrayList<>();

		while (!terminate) {
			try {
				if (reader.ready()) {
					// Read request header.
					String line = reader.readLine();
					while (line != null && line != "") {
						headerLines.add(line);
						line = reader.readLine();
					}

					// Parse request header
					RequestInfo info = parseHeader(headerLines);

					if (info.IsBadRequest()) {
						// Send bad request
						SendBadRequest(output);
					} else {

						switch (info.getCommand()) {
						case "GET":
							break;
						case "HEAD":
							break;
						case "POST":
							break;
						case "PUT":
							break;
						default:
							break;
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Parse the header of the request to get the desired info.
	 * 
	 * @param header The header to parse
	 * @return The data from the request header
	 */
	private RequestInfo parseHeader(ArrayList<String> header) {
		// Put header key values in hashmap
		HashMap<String, String> headerMap = new HashMap<>();
		for (int i = 1; i < header.size(); i++) {
			String[] pair = header.get(i).split(":");
			pair[0] = pair[0].toLowerCase();
			for (int j = 0; j < pair.length; j++) {
				pair[j] = pair[j].trim();
			}

			headerMap.put(pair[0], pair[1]);
		}
		String command = "";
		String path = "";
		boolean isBadRequest = false;

		// Parse first line
		String requestLine = header.get(0);
		command = requestLine.substring(0, requestLine.indexOf(" "));
		if (!RequestInfo.isValidCommand(command))
			isBadRequest = true;
		path = requestLine.substring(requestLine.indexOf(" ") + 1, requestLine.indexOf(" "));
		if (path.indexOf("/") != 0)
			isBadRequest = true;

		String http = requestLine.substring(requestLine.lastIndexOf(" ") + 1);
		if (!http.equals("HTTP/1.1"))
			isBadRequest = true;

		// Process header data
		int contentLength;
		ContentType type;
		boolean connectionclosed = false;

		// Check for host header
		if (!headerMap.containsKey("host"))
			isBadRequest = true;

		if (headerMap.containsKey("content-length"))
			contentLength = Integer.parseInt(headerMap.get("content-length"));
		else
			contentLength = 0;

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
			case "text/plain":
				type = ContentType.TEXT;
				break;
			default:
				type = ContentType.UNKNOWN;
				break;
			}
		} else {
			type = ContentType.UNKNOWN;
		}

		String date = "";
		String ifModifiedSince = "";

		if (headerMap.containsKey("date"))
			date = headerMap.get("date");

		if (headerMap.containsKey("if-modified-since"))
			ifModifiedSince = headerMap.get("if-modified-since");

		if (headerMap.containsKey("connection"))
			connectionclosed = (headerMap.get("connection") == "close");

		return new RequestInfo(command, path, date, ifModifiedSince, connectionclosed, isBadRequest, type);
	}

	/**
	 * 
	 * @param stream
	 */
	private void SendBadRequest(OutputStream stream) {
		PrintWriter writer = new PrintWriter(stream);
		ZonedDateTime now = ZonedDateTime.now();
		String date = now.format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String resp = "The request was not valid";

		writer.print("HTTP/1.1 400 Bad Request\r\n");
		writer.print("Content-Type: text/plain\r\n");
		writer.print("Date: " + date + "\r\n");
		writer.print("Content-Length: " + resp.length() + "\r\n");
		writer.print("\r\n");
		writer.println(resp);
	}

	/**
	 * 
	 */
	private void handePut() {
		// Save file
	}
	
	/**
	 * 
	 */
	private void handlePost() {
		// Append or save to file
	}
	
	/**
	 * 
	 */
	private void handleGet() {
		// get file and send
	}
	
	/**
	 * 
	 */
	private void handleHead() {
		// get file and send header
	}
	
	private void getResponseHeader(OutputStream stream, String code, String message) {
		PrintWriter writer = new PrintWriter(stream);
		ZonedDateTime now = ZonedDateTime.now();
		String date = now.format(DateTimeFormatter.RFC_1123_DATE_TIME);
		
		
	}
	
	/**
	 * 
	 */
	public void close() {
		this.terminate = true;
	}

}
