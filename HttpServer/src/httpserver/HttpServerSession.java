package httpserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.BufferOverflowException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.management.StandardEmitterMBean;

public class HttpServerSession extends Thread {

	/**
	 * 
	 */
	Socket socket;

	private boolean terminate;

	private HttpServer server;

	/**
	 * 
	 * @param socket
	 */
	public HttpServerSession(Socket socket, HttpServer server) throws IllegalArgumentException {
		if (socket == null) {
			throw new IllegalArgumentException("Given socket is not valid");
		}
		if (server == null)
			throw new IllegalArgumentException("Given server is not valid");
		this.socket = socket;
		this.terminate = false;
		this.server = server;
	}

	/**
	 * 
	 */
	@Override
	public void run() {
		InputStream input;
		OutputStream output;
		try {
			// set timeout to 2 seconds.
			this.socket.setSoTimeout(2 * 1000);
			input = this.socket.getInputStream();
			output = this.socket.getOutputStream();
		} catch (IOException e) {
			System.out.println("Failed to get streams");
			return;
		}

		while (!terminate) {
			try {
				ArrayList<String> headerLines = new ArrayList<>();
				// Read request header.
				String line = readLine(input);
				while (line != null && line != "") {
					headerLines.add(line);
					line = readLine(input);
				}
				if (!headerLines.isEmpty()) {
					System.out.println("HEADER:");
					for(String locline : headerLines)
						System.out.println(locline);
					System.out.println("");
					// Parse request header
					RequestInfo info = parseHeader(headerLines);

					if (info.IsBadRequest()) {
						// Send bad request
						byte[] resp = "The request was mallformed".getBytes();
						SendResponse(output, ResponseCode.BAD_REQUEST, "text/plain", resp, resp.length, false);
					} else {
						long size;
						switch (info.getCommand()) {
						case "HEAD":
							handleOutput(info, output, true);
							break;
						case "GET":
							handleOutput(info, output, false);
							break;
						case "POST":
							handeInput(info, output, input, false);
							break;
						case "PUT":
							handeInput(info, output, input, true);
							break;
						default:
							break;
						}
						if (info.getConnectionClose()) {
							// Close this socket.
							close();
						}
					}
				}

			} catch (SocketTimeoutException e) {
				// Do nothing
			} catch (IOException e) {
				close();
			}
		}
		// Close socket
		try {
			this.socket.close();
		} catch (IOException e) {
			System.out.println("Socket failed to close");
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
			String[] pair = header.get(i).split(":", 2);
			pair[0] = pair[0].toLowerCase();
			for (int j = 0; j < pair.length; j++) {
				pair[j] = pair[j].trim();
			}

			headerMap.put(pair[0], pair[1]);
		}

		// Valid standard values, if parsing the first line fails, these will be used
		// but request will be flagged as bad.
		String command = "HEAD";
		String path = "/";
		boolean isBadRequest = false;

		// Parse first line
		String requestLine = header.get(0);
		try {
			command = requestLine.substring(0, requestLine.indexOf(" "));
			if (!RequestInfo.isValidCommand(command))
				isBadRequest = true;
			requestLine = requestLine.substring(requestLine.indexOf(" ") + 1);
			path = requestLine.substring(0, requestLine.indexOf(" "));
			if (path.indexOf("/") != 0)
				isBadRequest = true;

			String http = requestLine.substring(requestLine.lastIndexOf(" ") + 1);
			if (!http.equals("HTTP/1.1"))
				isBadRequest = true;
		} catch (Exception e) {
			isBadRequest = true;
		}
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
			connectionclosed = (headerMap.get("connection") == "close" || headerMap.get("connection") == "Closed");

		return new RequestInfo(command, path, date, ifModifiedSince, connectionclosed, isBadRequest, type, contentLength);
	}

	/**
	 * 
	 * @param info
	 * @param writer
	 * @param headonly
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void handleOutput(RequestInfo info, OutputStream writer, boolean headonly)
			throws UnsupportedEncodingException, IOException {
		long size = -1;
		try {
			size = getFileSize(info.getPath());
		} catch (IllegalArgumentException ex) {		}
		if (size < 0) {
			byte[] resp = "File not found".getBytes();
			SendResponse(writer, ResponseCode.NOT_FOUND, "text/plain", resp, resp.length, false);
		} else {
			if (info.getModifiedSince() != "") {
				ZonedDateTime targetTime = ZonedDateTime
						.parse(info.getModifiedSince(), DateTimeFormatter.RFC_1123_DATE_TIME)
						.withZoneSameInstant(ZoneId.systemDefault());
				ZonedDateTime lastmodify = getFileLastModified(info.getPath());

				if (lastmodify.isAfter(targetTime)) {
					if (headonly) {
						SendResponse(writer, ResponseCode.SUCCESS, getFileContentType(info.getPath()), null, size,
								true);
					} else {
						byte[] body = readFile(info.getPath());
						if(body.length != size)
							System.out.println("Size error!!");
						SendResponse(writer, ResponseCode.SUCCESS, getFileContentType(info.getPath()), body, size,
								false);
					}
				} else {
					// File not modified
					SendEmptyResponse(writer, ResponseCode.NOT_MODIFIED);
				}
			} else {
				if (headonly) {
					SendResponse(writer, ResponseCode.SUCCESS, getFileContentType(info.getPath()), null, size, true);
				} else {
					byte[] body = readFile(info.getPath());
					if(body.length != size)
						System.out.println("Size error!!");
					SendResponse(writer, ResponseCode.SUCCESS, getFileContentType(info.getPath()), body, size, false);
				}
			}
		}
	}

	/**
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * 
	 */
	private void handeInput(RequestInfo info, OutputStream writer, InputStream reader, boolean newFile)
			throws UnsupportedEncodingException, IOException {
		// Read body
		byte[] result;
		try {
			ArrayList<Byte> body = new ArrayList<>();
			byte[] buffer = new byte[info.GetContentLength()];
			int count = reader.read(buffer, 0, info.GetContentLength());
			int rest = info.GetContentLength() - count;
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
			result = new byte[body.size()];
			for (int i = 0; i < body.size(); i++) {
				result[i] = bodyarray[i];
			}

			// Save file
			saveResource(result, info.getPath(), !newFile);

		} catch (Exception e) {
			// Send server error
			byte[] res = "Data write failed".getBytes();
			SendResponse(writer, ResponseCode.SERVER_ERROR, "text/plain", res, res.length, false);
			return;
		}

		// Send response
		if (newFile) {
			byte[] res = "A new file was created".getBytes();
			SendResponse(writer, ResponseCode.CREATED, "text/plain", res, res.length, false);
		} else {
			byte[] res = "Data succesfully appended".getBytes();
			SendResponse(writer, ResponseCode.SUCCESS, "text/plain", res, res.length, false);
		}
	}

	/**
	 * 
	 * @param stream
	 * @param responseCode
	 * @param responseMessage
	 * @param contentType
	 * @param body
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void SendResponse(OutputStream stream, ResponseCode code, String contentType, byte[] body, long bodylength,
			boolean headOnly) throws UnsupportedEncodingException, IOException {
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
		String date = now.format(DateTimeFormatter.RFC_1123_DATE_TIME);
		stream.write(("HTTP/1.1 " + Integer.toString(code.getCode()) + " " + code.getMessage() + "\r\n")
				.getBytes(Charset.forName("US-ASCII")));
		stream.write(("Date: " + date + "\r\n").getBytes(Charset.forName("US-ASCII")));
		if (body != null || headOnly) {
			stream.write(("Content-Type: " + contentType + "\r\n").getBytes(Charset.forName("US-ASCII")));
			stream.write(("Content-Length: " + Long.toString(bodylength) + "\r\n").getBytes(Charset.forName("US-ASCII")));
		}
		stream.write("\r\n".getBytes(Charset.forName("US-ASCII")));
		if (body != null)
			stream.write(body, 0, (int)bodylength);
	}

	/**
	 * 
	 * @param stream
	 * @param responseCode
	 * @param responseMessage
	 * @param contentType
	 * @param body
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void SendEmptyResponse(OutputStream stream, ResponseCode code)
			throws UnsupportedEncodingException, IOException {
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		String date = now.format(DateTimeFormatter.RFC_1123_DATE_TIME);
		stream.write(("HTTP/1.1 " + Integer.toString(code.getCode()) + " " + code.getMessage() + "\r\n")
				.getBytes(Charset.forName("US-ASCII")));
		stream.write(("Date: " + date + "\r\n").getBytes(Charset.forName("US-ASCII")));
		stream.write("\r\n".getBytes(Charset.forName("US-ASCII")));
	}

	/**
	 * 
	 */
	public void close() {
		this.terminate = true;
		this.server.deleteSession(this);
	}

	/**
	 * Reads one line from the stream
	 *
	 * @return The read line.
	 * @throws IOException The read from the host failed.
	 */
	protected String readLine(InputStream reader) throws SocketTimeoutException, IOException {
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
	 * Saves the given dataset to disk.
	 * 
	 * @param resource The dataset to save.
	 * @param path     The path to save to, including file name and extension
	 * @throws IOException           The file couldn't be written to.
	 * @throws FileNotFoundException The file couldn't be opened or created.
	 */
	private void saveResource(byte[] data, String path, boolean append) throws IOException, FileNotFoundException {
		String dirpath = "webpage/" + path.substring(1, path.lastIndexOf("/") + 1);
		File dir = new File(dirpath);
		if (!dir.exists())
			dir.mkdirs();

		FileOutputStream filewriter = new FileOutputStream("webpage" + path, append);
		filewriter.write(data);
		filewriter.flush();
		filewriter.close();
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private long getFileSize(String path) {
		if (path == null || path.indexOf("/") != 0)
			throw new IllegalArgumentException("given file path is not valid");
		File file = new File("webpage" + path);
		if (!file.exists() || !file.isFile())
			return -1;
		return file.length();
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private ZonedDateTime getFileLastModified(String path) throws IllegalArgumentException {
		if (path == null || path.indexOf("/") != 0)
			throw new IllegalArgumentException("given file path is not valid");
		File file = new File("webpage" + path);
		if (!file.exists() || !file.isFile())
			return ZonedDateTime.now();

		long timeSinceEpoch = file.lastModified();
		Instant i = Instant.ofEpochMilli(timeSinceEpoch);
		return ZonedDateTime.ofInstant(i, ZoneId.systemDefault());
	}

	private byte[] readFile(String path) throws IOException {
		if (path == null || path.indexOf("/") != 0)
			throw new IllegalArgumentException("given file path is not valid");

		return Files.readAllBytes(Path.of("webpage" + path));
	}

	private String getFileContentType(String path) {
		if (path == null || path.indexOf("/") != 0)
			throw new IllegalArgumentException("given file path is not valid");
		File file = new File("webpage" + path);
		if (!file.exists() || !file.isFile())
			return "";
		String result;
		switch (file.getName().substring(file.getName().lastIndexOf(".") + 1)) {
		case "html":
			result = "text/html";
			break;
		case "txt":
			result = "text/plain";
			break;
		case "png":
			result = "image/png";
			break;
		case "jpg":
			result = "image/jpg";
			break;
		case "gif":
			result = "image/gif";
			break;
		default:
			result = "application/octet-stream";
			break;
		}
		return result;
	}
}
