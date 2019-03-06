package htmlclient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
			uri = new URI("http://" + args[1]);
			port = Integer.parseInt(args[2]);
		} else {
			System.out.println("Not the correct input arguments!");
			System.out.println("Usage:");
			System.out.println("\tHtmlClientTest HTTPCommand URI");
			System.out.println("\tHtmlClientTest HTTPCommand URI Port");
			return;
		}
		
		HTTPSession session = new HTTPSession(uri.getHost(), port);
		
		if (!session.openConnection()) {
			System.out.println("Connection failed");
			return;
		}
		
		if(!session.sendCommand(command, uri.getPath())) {
			System.out.println("Command failed");
			return;
		}
		
		if(!session.closeConnection()) {
			System.out.println("Connection closed failed");
			return;
		}
		
		return;
	}

}
