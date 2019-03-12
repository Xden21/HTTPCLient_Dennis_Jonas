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
			// TODO Auto-generated catch block
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
	
	public static void main(String[] args) {
		HttpServer server = new HttpServer(5000);
	}
}
