import java.io.*;
import java.net.*;

/**
 * Client class for the group chat.
 * 
 * Can be run with terminal command: java Client <ip_addr> <port>
 * 
 * @author Ryan LeBoeuf
 */
public class Client {

	// class variables
    private static boolean running;
	private static String username;

	/**
	 * Continuously waits for System.in input and then sends that input to the destination socket
	 * @param destination Socket to send input to
	 */
	private static void waitForSystemInput(Socket destination) {
		try {

			// read from the console and write to the socket
			BufferedReader fromUserReader = new BufferedReader(
				new InputStreamReader(System.in)
			);
			PrintWriter destSocketWriter = new PrintWriter(destination.getOutputStream(), true);

			// prompt for username, prepended to all messages sent by the client
			System.out.print("Enter your username: ");
			username = fromUserReader.readLine();
			System.out.println("Welcome, " + username + "!");

			// main loop, continuously read from System.in and send msg to socket
			while(true) {
				String line = fromUserReader.readLine().trim();	// don't want leading or trailing whitespace
	
				// null means user is done, exit program
				if (line == null) {
					System.out.println("Closing connection...");
					break;
				}
	
				// send message to the socket
				destSocketWriter.println(username + ": " + line);
			}

			running = false;
			destSocketWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
            running = false;
		}
	}

	/**
	 * Continuously wait for input from source socket and print input to console
	 * @param source Socket to receive input from
	 */
	private static void waitForSocketInput(Socket source) {
		try {

			// read from the socket
			BufferedReader sourceSocketReader = new BufferedReader(
				new InputStreamReader(source.getInputStream())
			);
	
			// contiuously read from source, print to server console
			while (true) {
				String sourceLine;
	
				// null means source is done, exit
				if ((sourceLine = sourceSocketReader.readLine()) != null) {
					System.out.println(sourceLine);
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
			System.out.println("usage: java Client <ip_addr> <port>");
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

			// wait for system input on separate thread
			new Thread(() -> {
				waitForSystemInput(sock);
			}).start();

			// wait for socket input on separate thread
			new Thread(() -> {
				waitForSocketInput(sock);
			}).start();

			// sit and spin
			while(running) {}

			// close the socket and exit
			sock.close();

		} catch(Exception e) {
			System.out.println(e);
		}

		System.exit(0);
    }
}
