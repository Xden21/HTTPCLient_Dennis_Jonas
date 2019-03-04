package htmlclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * The main class, where the html client starts from.
 * @author Dennis Debree
 */
public final class Main {

	/**
	 * The main function. this is the entry point of the program.
	 * @param args:	The command line arguments of this program. There should be either 2 or 3.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String command;
		URI uri;
		int port;
		
		if (args.length == 2) {
			command = args[0];
			uri = new URI("http://" + args[1]);
			port = 80;
		} else if (args.length == 3) {
			command = args[0];
			uri = new URI("http://" +  args[1]);
			port = Integer.parseInt(args[2]);
		} else {
			System.out.println("Not the correct input arguments!");
			System.out.println("Usage:");
			System.out.println("\tHtmlClientTest HTTPCommand URI");
			System.out.println("\tHtmlClientTest HTTPCommand URI Port");
			return;
		}
		
		switch (command) {
		case "GET":			
			break;
		case "PUT":
			break;
		case "POST":
			break;
		default:
			break;
		}

		Socket clientSocket = new Socket(uri.getHost(), port);

		PrintWriter toServer = new PrintWriter(clientSocket.getOutputStream());

		BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), Charset.forName("ISO-8859-1")));

		toServer.print("GET " + uri.getPath() + " HTTP/1.1\r\n");
		toServer.print("Host: " + uri.getHost() + "\r\n");
		toServer.print("\r\n");
		toServer.flush();

		String line;
		while ((line = fromServer.readLine()) != null) {
			System.out.println(line);
		}
		clientSocket.close();
	}	

}
