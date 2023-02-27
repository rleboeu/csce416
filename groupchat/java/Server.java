import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {

    private static boolean running;
    private static ArrayList<Socket> connectedClients = new ArrayList<>();

    private static void waitForInput() {
        try {
			BufferedReader fromUserReader = new BufferedReader(
				new InputStreamReader(System.in)
			);

			while(true) {
				String line = fromUserReader.readLine();
	
				// If we get null, it means user is done
				if (line == null) {
					System.out.println("Closing connection");
					break;
				}
	
				// Send the line to the client
				broadcast(null, "Server: " + line);
			}

			running = false;
		} catch (Exception e) {
			e.printStackTrace();
            System.exit(1);
		}
    }

    private static void broadcast(Socket source, String message) {
        boolean shouldCompare = (source != null);

        for (Socket client : connectedClients) {
            if (client.isConnected() == false) {
                connectedClients.remove(client);
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (shouldCompare && client == source) {
                        continue;
                    }

                    PrintWriter toClientWriter = new PrintWriter(client.getOutputStream(), true);
                    toClientWriter.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // private static void broadcast(String message) {
    //     for (Socket client : connectedClients) {
    //         if (client.isConnected() == false) {
    //             connectedClients.remove(client);
    //             try {
    //                 client.close();
    //             } catch (IOException e) {
    //                 e.printStackTrace();
    //             }
    //         } else {
    //             try {
    //                 PrintWriter toClientWriter = new PrintWriter(client.getOutputStream(), true);
    //                 toClientWriter.println(message);
    //             } catch (IOException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    // }

    private static void waitForClient(Socket onSocket) {
        try {
            BufferedReader fromClientReader = new BufferedReader(
                new InputStreamReader(onSocket.getInputStream())
            );

            while (true) {
                String clientLine;

                if ((clientLine = fromClientReader.readLine()) != null) {
                    System.out.println(clientLine);
                    broadcast(onSocket, clientLine);
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

            Thread serverInputThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    waitForInput();
                }
            });

            serverInputThread.start();

			// Keep serving the client
			while (running) {
                Socket clientSock = serverSock.accept();
                connectedClients.add(clientSock);

                System.out.println("Connected to a client at ('" +
                    ((InetSocketAddress) clientSock.getRemoteSocketAddress()).getAddress().getHostAddress()
                    + "', '" +
                    ((InetSocketAddress) clientSock.getRemoteSocketAddress()).getPort()
                    + "')"
                );

                Thread clientInputThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        waitForClient(clientSock);
                    }
                });

                clientInputThread.start();
            }

            serverSock.close();
		} catch(Exception e) {
			// Print the exception message
			System.out.println(e);
		}
    }
}
