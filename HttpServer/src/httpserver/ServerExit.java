package httpserver;

/**
 * Class to shut down the server
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public class ServerExit extends Thread {

	/**
	 * The server to shut down
	 */
	private HttpServer server;
	
	/**
	 * Constructs the thread to shut down the server
	 * 
	 * @param server the server to shut down
	 * @throws IllegalArgumentException the given server is not valid
	 */
	public ServerExit(HttpServer server) throws IllegalArgumentException {
		// TODO Auto-generated constructor stub
		if(server == null)
			throw new IllegalArgumentException();
		
		this.server = server;
	}

	/**
	 * Shut down the server
	 */
	@Override
	public void run() {
		this.server.ShutDown();
	}

}
