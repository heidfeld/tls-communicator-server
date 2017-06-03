package server;

import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import entity.ChatMessage;
import gui.ServerGUI;

public class Server {

	private static int id;
	private ArrayList<ClientThread> clientThreads;
	private ServerGUI serverGui;
	private SimpleDateFormat sdf;
	private int port;
	private boolean keepGoing;

	public Server(int port) {
		this(port, null);
	}

	public Server(int port, ServerGUI sg) {
		this.serverGui = sg;
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		clientThreads = new ArrayList<ClientThread>();
	}

	public void start() {
		keepGoing = true;
		try {
			ServerSocket serverSocket = generateServerSocket();

			while (keepGoing) {
				display("Server waiting for Clients on port " + port + ".");

				Socket socket = serverSocket.accept();
				if (!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);
				clientThreads.add(t);
				t.start();
			}
			try {
				serverSocket.close();
				for (int i = 0; i < clientThreads.size(); ++i) {
					ClientThread tc = clientThreads.get(i);
					try {
						tc.input.close();
						tc.output.close();
						tc.socket.close();
					} catch (IOException ioE) {
						// not much I can do
					}
				}
			} catch (Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (Exception e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}

	private ServerSocket generateServerSocket() throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException, FileNotFoundException, UnrecoverableKeyException, KeyManagementException {
		char[] password = { 't', 'e', 's', 't', '1', '2', '3' };
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
		ServerSocket serverSocket = factory.createServerSocket(port);
		return serverSocket;
	}

	public void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		} catch (Exception e) {
			// nothing I can really do
		}
	}

	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if (serverGui == null)
			System.out.println(time);
		else
			serverGui.appendEvent(time + "\n");
	}

	private synchronized void broadcast(String message) {
		String time = sdf.format(new Date());
		String messageFromat = time + " " + message + "\n";
		if (serverGui == null)
			System.out.print(messageFromat);
		else
			serverGui.appendRoom(messageFromat);

		for (int i = clientThreads.size(); --i >= 0;) {
			ClientThread ct = clientThreads.get(i);
			if (!ct.writeMessage(messageFromat)) {
				clientThreads.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	synchronized void remove(int clientId) {
		for (int i = 0; i < clientThreads.size(); ++i) {
			ClientThread ct = clientThreads.get(i);
			if (ct.uniqueId == clientId) {
				clientThreads.remove(i);
				return;
			}
		}
	}

	public static void main(String[] args) {
		int portNumber = 1500;
		switch (args.length) {
		case 1:
			try {
				portNumber = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: > java Server [portNumber]");
				return;
			}
		case 0:
			break;
		default:
			System.out.println("Usage is: > java Server [portNumber]");
			return;

		}
		Server server = new Server(portNumber);
		server.start();
	}

	class ClientThread extends Thread {
		Socket socket;
		ObjectInputStream input;
		ObjectOutputStream output;
		int uniqueId;
		String username;
		ChatMessage chatMessage;
		String date;

		ClientThread(Socket socket) {
			uniqueId = ++id;
			this.socket = socket;
			System.out.println("Thread trying to create Object Input/Output Streams");
			try {
				output = new ObjectOutputStream(socket.getOutputStream());
				input = new ObjectInputStream(socket.getInputStream());
				username = (String) input.readObject();
				display(username + " connected.");
			} catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			} catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}

		public void run() {
			boolean isAlive = true;
			while (isAlive) {
				try {
					chatMessage = (ChatMessage) input.readObject();
				} catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;
				} catch (ClassNotFoundException e2) {
					break;
				}
				String message = chatMessage.getMessage();

				switch (chatMessage.getType()) {
				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					isAlive = false;
					break;
				case ChatMessage.WHOISIN:
					writeMessage("List of the users connected at " + sdf.format(new Date()) + "\n");
					for (int i = 0; i < clientThreads.size(); ++i) {
						ClientThread ct = clientThreads.get(i);
						writeMessage((i + 1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}
			}
			remove(uniqueId);
			close();
		}

		private void close() {
			try {
				if (output != null)
					output.close();
			} catch (Exception e) {
			}
			try {
				if (input != null)
					input.close();
			} catch (Exception e) {
			}
			;
			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
			}
		}

		private boolean writeMessage(String msg) {
			if (!socket.isConnected()) {
				close();
				return false;
			}
			try {
				output.writeObject(msg);
			}
			catch (IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}
