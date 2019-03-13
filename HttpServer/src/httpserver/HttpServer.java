package httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 
 * @author denni
 *
 */
public class HttpServer {

	ServerSocket socket;
	
	ArrayList<HttpServerSession> currentSessions;
	
	/**
	 * 
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
	 * 
	 * @throws IOException
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
	
	public void deleteSession(HttpServerSession session) {
		this.currentSessions.remove(session);
	}
	
	public void ShutDown() {
		for(int i = currentSessions.size() - 1; i >= 0; i--) {
			currentSessions.get(i).close();
		}
	}	
}
