package httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The main class, where the html client starts from.
 *
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public final class Main {

	/**
	 * The main function. this is the entry point of the program.
	 * Takes a command line input and parses it into usable parts.
	 * 
	 * @param args:	The command line arguments of this program. There should be either 2 or 3.
	 */
	public static void main(String[] args)  {
		String command;
		URI uri;
		int port;

		if (args.length == 2) {
			command = args[0];
			try {
				String uriStr = args[1];
				if(uriStr.indexOf("/") == -1)
					uriStr = uriStr + "/";
				uri = new URI("http://" + uriStr);
			} catch (URISyntaxException e) {
				System.out.println("Given URI is in wrong format.");
				return;
			}
			port = 80;
		} else if (args.length == 3) {
			command = args[0];
			try {
				String uriStr = args[1];
				if(uriStr.indexOf("/") == -1)
					uriStr = uriStr + "/";
				uri = new URI("http://" + uriStr);
			} catch (URISyntaxException e) {
				System.out.println("Given URI is in wrong format.");
				return;
			}
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
		try {
		if(!session.sendCommand(command, uri.getPath())) {
			System.out.println("Command failed");
			return;
		}
		}
		catch(UnsupportedOperationException e1) {
			System.out.println(e1.getMessage());
		}
		catch(IOException e2) {
			System.out.println(e2.getMessage());
		}
		
		if(!session.closeConnection()) {
			System.out.println("Connection closed failed");
			return;
		}
		
		return;
	}

}
