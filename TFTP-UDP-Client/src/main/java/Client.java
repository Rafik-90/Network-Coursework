import client.TFTPClient;
import Exceptions.TFTPException;



/**
 * Main class for running the TFTP client application.
 * This class initializes the TFTP client and starts the command line interface for file transfers.
 */
public class Client {


	/**
	 * Main method to launch the TFTP client application.
	 * It sets up the client with a specified server address and port, then runs the command line interface.
	 *
	 * @param args Command line arguments (not used in this application).
	 */
	public static void main(String[] args) {

		// Create an instance of TFTPClient pointing to "localhost" on port 8888
		TFTPClient client = new TFTPClient("localhost", 8888);
		// Initialize the command line interface with the created TFTPClient
		CommandLine commandLine = new CommandLine(client);
		try {
			// Run the command line interface to handle user commands
			commandLine.run();
		} catch (Exception e) {
			// Print any exceptions that occur during the operation of the command line
			e.printStackTrace();
			// Exit the program with an error status (1) if an exception is caught
			System.exit(1);
		}
	}
}
