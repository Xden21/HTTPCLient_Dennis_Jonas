package httpserver;

public class ServerExit extends Thread {

	private HttpServer server;
	
	public ServerExit(HttpServer server) throws IllegalArgumentException {
		// TODO Auto-generated constructor stub
		if(server == null)
			throw new IllegalArgumentException();
		
		this.server = server;
	}

	@Override
	public void run() {
		this.server.ShutDown();
	}

}
