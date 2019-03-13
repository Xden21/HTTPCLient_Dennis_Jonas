package httpserver;

import java.io.IOException;

public class Main {

	public Main() {}
	
	public static void main(String[] args) {
		HttpServer server = new HttpServer(5000);
		try {
			System.out.println("Starting server");
			server.Start();
		} catch (IOException e) {
			System.out.println("Server failed");
		}
	}

}
