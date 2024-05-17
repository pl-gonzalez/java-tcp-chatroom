import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Runnanble means that can be pass to a  thread or thread pool
public class Server implements Runnable {
	
	private ArrayList<ConnectionHandler> connections;
	private boolean done;
	private ServerSocket server;
	
	//thread Pool -> 
	private ExecutorService pool;
	
	public Server() {
		connections = new ArrayList<>();
		done = false;
		
	}
	
	@Override
	public void run() {
		try {
			server = new ServerSocket(9999);
			pool = Executors.newCachedThreadPool();
			
			while(!done) {
				Socket client = server.accept();
				ConnectionHandler handler = new ConnectionHandler(client);
				connections.add(handler);
				pool.execute(handler);
			}
			
		} catch (IOException e) {
			shutdown();
		}
		
	}
	
	public void broadcast (String message) {
		for(ConnectionHandler ch : connections) {
			if(ch != null) {
				ch.sendMessage(message);
			}
		}
	}
	
	public void shutdown( ) {
		try {
			done = true;
			if(!server.isClosed()) {
				server.close();
			}
			
			for(ConnectionHandler ch : connections) {
				ch.shutdown();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	// inner class
	class ConnectionHandler implements Runnable {
		private Socket client;
		// Get messages from the client (from the socket)
		private BufferedReader in;
		// Write something to the client
		private PrintWriter out;
		private String nickname;
		
		//Constructor -> 
		public ConnectionHandler (Socket client) {
			this.client = client;
			
		}
		
		@Override
		public void run() {
			try {
				out = new PrintWriter(client.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out.println("Enter your nickname: ");
				nickname = in.readLine();
				System.out.println(nickname + " connected!");
				broadcast(nickname + " joined the chat!");
				String message;
				
				while ((message = in.readLine()) != null) {
					if(message.startsWith("/nick")) {
						String[] messageSplit = message.split("", 2);
						if(messageSplit.length == 2) {
							broadcast(nickname + "renamed themselves to " + messageSplit[1]);
							System.out.println(nickname + "renamed themselves to " + messageSplit[1]);
							nickname = messageSplit[1];
							out.println("Nickname changed: " + nickname);
						}
						else {
							out.println("No nickname provided");
						}
					}
					else if(message.startsWith("/quit")) {
						broadcast(nickname + "Left the chat");
					}
					else {
						broadcast(nickname + ":" + message);
					}
				}
			} catch (Exception e) {
				System.out.println("No users");
				shutdown();
			}
		}
		
		public void sendMessage(String message) {
			out.println(message);
		}
		
		public void shutdown() {
			try {
				in.close();
				out.close();
				if(!client.isClosed()) {
					client.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
			}
			
			}
		public static void main(String[] args) {
			System.out.println("Server is running!");
			Server server = new Server();
			server.run();
			
		}
		
		}
	
	}



