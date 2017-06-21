import com.sun.net.httpserver.HttpServer;
import handlers.RequestHandler;

import java.net.InetSocketAddress;

/**
 * Created by giggiux on 06/05/2017.
 */
public class Main {


	public static void main(String[] args) {
		try {
			int port = Integer.parseInt(System.getenv("PORT"));
			HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
			System.out.println("server started at " + port);
			server.createContext("/", new RequestHandler());
			server.setExecutor(null);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
