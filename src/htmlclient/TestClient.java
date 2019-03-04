package htmlclient;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * The html client test class. This serves as a baseline for the html client.
 * 
 * @author Dennis Debree
 *
 */
public class TestClient {

	public static void main(String[] args) throws Exception {
		String command;
		URI uri;
		int port;
		Command httpCommand;

		if (args.length == 2) {
			command = args[0];
			uri = new URI("http://" + args[1]);
			port = 80;
		} else if (args.length == 3) {
			command = args[0];
			uri = new URI("http://" + args[1]);
			port = Integer.parseInt(args[2]);
		} else {
			System.out.println("Not the correct input arguments!");
			System.out.println("Usage:");
			System.out.println("\tHtmlClientTest HTTPCommand URI");
			System.out.println("\tHtmlClientTest HTTPCommand URI Port");
			return;
		}

		Socket clientSocket = new Socket(uri.getHost(), port);

		OutputStream toServer = clientSocket.getOutputStream();

		InputStream fromServer = clientSocket.getInputStream();

		switch (command) {
		case "GET":
			httpCommand = new GetCommand(uri.getHost(), uri.getPath(), toServer, fromServer);
			break;
		case "PUT":
			clientSocket.close();
			return;
			//break;
		case "POST":
			clientSocket.close();
			return;
			//break;
		default:
			clientSocket.close();
			return;
			//break;
		}		
		
		httpCommand.executeCommand();
		
		System.out.println(httpCommand.getResponse());
		
		PrintWriter out = new PrintWriter(uri.getHost() + ".txt");
		
		out.print(httpCommand.getResponse());
		
		out.close();
		
		toServer.close();
		
		fromServer.close();
		
		clientSocket.close();
	}

}
