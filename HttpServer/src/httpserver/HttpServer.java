package httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * A class for starting an http server that hosts a web page.
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class HttpServer {

	/**
	 * The socket used to communicate to this http server.
	 */
	ServerSocket socket;
	
	/**
	 * A list keeping track of all current running sessions.
	 */
	ArrayList<HttpServerSession> currentSessions;
	
	/**
	 * Constructs a new http server
	 * 
	 * @param port the port for this server
	 */
	public HttpServer(int port) {
		try {
			this.socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		currentSessions = new ArrayList<>();
	}

	/**
	 * Starts the http server
	 * 
	 * @throws IOException starting the server socket failed
	 */
	public void Start() throws IOException {
		while(true) {
			System.out.println("Server started");
			Socket incomingConnection = this.socket.accept();
			if(incomingConnection != null) {
				HttpServerSession session = new HttpServerSession(incomingConnection, this);
				session.start();
				currentSessions.add(session);
			}
		}
	}
	
	/**
	 * Deletes a session from the active sessions list
	 * 
	 * @param session the session to remove
	 */
	public void deleteSession(HttpServerSession session) {
		this.currentSessions.remove(session);
	}
	
	/**
	 * Shots down the server
	 */
	public void ShutDown() {
		for(int i = currentSessions.size() - 1; i >= 0; i--) {
			currentSessions.get(i).close();
		}
	}	
}
