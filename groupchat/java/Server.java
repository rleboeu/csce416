import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Server class for the group chat
 * 
 * @author Ryan LeBoeuf
 * 
 * Can be run with terminal command: java Server <port>
 */
public class Server {

    // class variables
    private static boolean running;
    private static ArrayList<Socket> connectedClients = new ArrayList<>();
    private static final int MAX_CLIENT_CONNECTIONS = 10;

    /**
     * Continuously waits for System.in input and then broadcasts that input to clients
     */
    private static void waitForSystemInput() {
        try {
            // read input from system
			BufferedReader fromUserReader = new BufferedReader(
				new InputStreamReader(System.in)
			);

			while(true) {
				String line = fromUserReader.readLine().trim(); // no trailing or leading whitespace
	
				// If we get null, it means system is done
				if (line == null) {
					System.out.println("Closing connection");
					break;
				}
	
				// Send the line to the clients
				broadcast(null, "Server: " + line);
			}

			running = false;
		} catch (Exception e) {
			e.printStackTrace();
            System.exit(1);
		}
    }

    /**
     * Broadcast message received from source socket to all connected clients
     * @param source Socket that the message originates from. Is null iff the source is the Server.
     * @param message String to broadcast to all client.
     */
    private static void broadcast(Socket source, String message) {

        // for each connected client
        for (Socket client : connectedClients) {
            // if the client is not connected anymore, remove them from the clients list and close the socket
            if (client.isConnected() == false) {
                connectedClients.remove(client);

                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                // client is connected, broadcast to the client iff client != source
                try {
                    if (source != null && client == source) {
                        continue;
                    }

                    // send message
                    PrintWriter toClientWriter = new PrintWriter(client.getOutputStream(), true);
                    toClientWriter.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Continuously wait for input from a client, then print that message to the server and broadcast to other clients
     * @param client Socket that the client is connected via
     */
    private static void waitForClient(Socket client) {
        try {
            // read from client
            BufferedReader fromClientReader = new BufferedReader(
                new InputStreamReader(client.getInputStream())
            );

            while (true) {
                String clientLine;

                // if the client has something to say, broadcast message
                if ((clientLine = fromClientReader.readLine()) != null) {
                    System.out.println(clientLine);
                    broadcast(client, clientLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            running = false;
        }
    }

    public static void main(String[] args) {
        running = true;

        // Server needs the port number to listen on
		if (args.length != 1) {
			System.out.println("usage: java Server <port>");
			System.exit(1);
		}

		// Get the port on which server should listen */
		int serverPort = Integer.parseInt(args[0]);

		// Be prepared to catch socket related exceptions
		try {
			// Create a server socket with the given port
			ServerSocket serverSock = new ServerSocket(serverPort);
            
            System.out.println("Started Server on port " + serverPort);

            // wait for the system input on a separate thread
            new Thread(() -> {
                waitForSystemInput();
            }).start();

			// new connections
			while (running && connectedClients.size() <= MAX_CLIENT_CONNECTIONS) {

                // accept the connection and add to client list
                Socket clientSock = serverSock.accept();
                connectedClients.add(clientSock);

                // notify server of a new connection
                System.out.println("Connected to a client at ('" +
                    ((InetSocketAddress) clientSock.getRemoteSocketAddress()).getAddress().getHostAddress()
                    + "', '" +
                    ((InetSocketAddress) clientSock.getRemoteSocketAddress()).getPort()
                    + "')"
                );

                // wait for the client input on a new thread
                new Thread(() -> {
                    waitForClient(clientSock);
                }).start();

            }

            serverSock.close();
		} catch(Exception e) {
			e.printStackTrace();
            running = false;
		}
    }
}
