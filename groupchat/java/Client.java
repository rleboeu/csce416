import java.io.*;
import java.net.*;

public class Client {

    private static boolean running;
	private static String username;

	/**
	 * Wait for input from System.in and then send it to the user
	 * @param onSocket Socket to output to
	 */
	private static void waitForInput(Socket onSocket) {
		try {
			BufferedReader fromUserReader = new BufferedReader(
				new InputStreamReader(System.in)
			);
			PrintWriter toServerWriter = new PrintWriter(onSocket.getOutputStream(), true);

			System.out.print("Enter your username: ");
			username = fromUserReader.readLine();

			while(true) {
				String line = fromUserReader.readLine();
	
				// If we get null, it means user is done
				if (line == null) {
					System.out.println("Closing connection");
					break;
				}
	
				// Send the line to the server
				toServerWriter.println(username + ": " + line);
			}

			running = false;
			toServerWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
            running = false;
		}
	}

	private static void waitForServer(Socket onSocket) {
		try {
			BufferedReader fromServerReader = new BufferedReader(
				new InputStreamReader(onSocket.getInputStream())
			);
	
			while (true) {
				String servLine;
	
				if ((servLine = fromServerReader.readLine()) != null) {
					System.out.println(servLine);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
            running = false;
		}
	}

    public static void main(String[] args) {
		running = true;

        // Client needs server's contact information
		if (args.length != 2) {
			System.out.println("usage: java Client <server name> <server port>");
			System.exit(1);
		}

		// Get server's whereabouts
		String serverName = args[0];
		int serverPort = Integer.parseInt(args[1]);

		// Be prepared to catch socket related exceptions
		try {
			// Connect to the server at the given host and port
			Socket sock = new Socket(serverName, serverPort);
			System.out.println(
				"Connected to server at ('" + serverName + "', '" + serverPort + "'");

			Thread userInputThread = new Thread(new Runnable() {
				@Override
				public void run() {
					waitForInput(sock);
				}
			});
			userInputThread.start();

			
			Thread serverInputThread = new Thread(new Runnable() {
				@Override
				public void run() {
					waitForServer(sock);
				}
			});
			serverInputThread.start();

			while(running) {}

			// close the socket and exit
			userInputThread.interrupt();
			serverInputThread.interrupt();
			sock.close();

		}
		catch(Exception e) {
			System.out.println(e);
		}

		System.exit(0);
    }
}
