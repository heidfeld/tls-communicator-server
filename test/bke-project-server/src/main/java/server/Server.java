package server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

public class Server {
	public static void main(String[] args) throws Exception {
		char[] password = { 't', 'e', 's', 't', '1', '2', '3'};
		KeyStore ks = KeyStore.getInstance("PKCS12");
		try (InputStream input = new FileInputStream("server.pfx")) {
			ks.load(input, password);
		}

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
		kmf.init(ks, password);
		KeyManager[] keyManagers = kmf.getKeyManagers();

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagers, null, null);

		SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
		ServerSocket server = factory.createServerSocket(12345);

		//Command to server should start with # and ends with #
		Protocol protocol = new Protocol();
		while (true) {
			try (Socket socket = server.accept()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				String command = reader.readLine();
				if(command.length() > 0) {
					System.out.println(command);
					String response = protocol.getServerResponseForCommand(command);
					writer.write(response);
					System.out.println(response);
				}
			}
		}

	}
}
