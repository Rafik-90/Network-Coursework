import Exceptions.TFTPException;
import client.TFTPClient;

import java.util.Scanner;

/* Implementation of our command line interface that will interact with our client class so the user may upload and retrieve files.
 */
public class CommandLine {


	private enum Command {
		// Commands in the client side
		TRANSFER,
		RECEIVE,
		HELP,
		EXIT
	}


	Scanner scanner;
	TFTPClient client;

	public CommandLine(TFTPClient client) {
		this.client = client;
		this.scanner = new Scanner(System.in);
	}

	public void run() throws TFTPException {
		System.out.println("===========================================================================");
		System.out.println("Welcome to QuickSync File Hub - Streamlined Transfers at Your Fingertips!");
		System.out.println("===========================================================================");
		System.out.println("choose a command: ");
		System.out.println("===================");
		System.out.println(help());
		while (true) {
			// Parse the command into a command and filename or print the help message if the command is invalid
			System.out.print("Enter command: ");
			CommandPair cmdPair = parseCommand(scanner.nextLine());
			doCommand(cmdPair.getCmd(), cmdPair.getFilename());
		}
	}

	private void doCommand(Command command, String filename) throws TFTPException {
		switch (command) {
		case TRANSFER:
			doUpload(filename);
			break;
		case RECEIVE:
			doDownload(filename);
			break;
		case HELP:
			System.out.println(help());
			break;
		case EXIT:
			System.exit(0);
			break;
		default:
			System.err.println("Invalid command");
			System.out.println(help());
			break;
		}
	}

	private void doUpload(String filename) {
		client.transmitFile(filename);
	}

	private void doDownload(String filename) throws TFTPException {
		client.receiveFile(filename);
	}

	public static String help() {
		return "Menu: \n" +
						"\ttransfer <filename> - Command that transfers the file to the server\n" +
						"\treceive <filename> - Command that receives the file from the server\n" +
						"\texit - Command that exits the program\n" +
						"\thelp - Command that prints this help message again\n";
	}

	// Parses a command into a Command enum and a filename.
	public CommandPair parseCommand(String command) {
		String[] parts = command.split(" ");

		// Attempt to parse the command into a Command enum
		Command command1;
		try {
			command1 = Command.valueOf(parts[0].toUpperCase().trim());
		} catch (Exception e) {
			return new CommandPair(Command.HELP);
		}

		// If the command is upload or download, we need to parse the filename
		if (command1 == Command.TRANSFER || command1 == Command.RECEIVE) {
			String filename = parts[1];
			return new CommandPair(command1, filename);
		}
		return new CommandPair(command1);
	}


	// Pair consisting of a command and its arguments
	public class CommandPair {
		private final Command cmd;
		private final String filename;

		//constructor1
		public CommandPair(Command cmd, String filename) {
			this.cmd = cmd;
			this.filename = filename;
		}

		//constructor2
		public CommandPair(Command cmd) {
			this.cmd = cmd;
			this.filename = null;
		}

		public Command getCmd() {
			return cmd;
		}

		public String getFilename() {
			return filename;
		}

		@Override
		public String toString() {
			return cmd.toString() + " " + filename;
		}
	}






}
