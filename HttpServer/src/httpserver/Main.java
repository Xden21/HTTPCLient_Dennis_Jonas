package httpserver;

import java.io.IOException;

/**
 * The main class where this program starts
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class Main {
	
	/**
	 * Starts the server
	 * 
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		HttpServer server = new HttpServer(80);
		Runtime.getRuntime().addShutdownHook(new ServerExit(server));
		try {
			System.out.println("Starting server");
			server.Start();
		} catch (IOException e) {
			System.out.println("Server failed");
		}
	}

}
