import client.TFTPClient;
import Exceptions.TFTPException;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLOutput;
import java.util.Scanner;

/**
 * Implementation of a command line interface that interacts with the TFTPClient
 * class, allowing users to upload and retrieve files.
 */
public class CommandLine {

    private final Scanner scanner; // Scanner to read user input from the command line
    private final String serverAddress;// Address of the TFTP server
    private final int serverPort;// Port on which the TFTP server is listening
    private TFTPClient client;// Client to handle TFTP operations


    /**
     * Constructs a new command line interface with specified server address and port.
     * @param serverAddr the IP address of the TFTP server
     * @param serverPort the port number of the TFTP server
     */
    public CommandLine(String serverAddr, int serverPort) {
        this.scanner = new Scanner(System.in);
        this.serverAddress = serverAddr;
        this.serverPort = serverPort;
    }


    /**
     * Initializes the TFTP client by creating a socket connection to the server.
     * If the connection cannot be established, the program will exit.
     */
    private void initializeClient() {
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            client = new TFTPClient(socket);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
            System.exit(1);
        } catch (IOException | TFTPException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Runs the main program loop, accepting user commands and processing them.
     * Continuously prompts for and executes commands until an exit command is issued.
     */
    public void runProgram() {

        System.out.println("======================================================");
        System.out.println("Transform Your File Transfer Process with Our Program!");
        System.out.println("======================================================");
       // System.out.println();
        System.out.println("Choose Option below: ");
        System.out.println("=====================");
        System.out.println(menu());
        while (true) {
            initializeClient();
            System.out.print("Enter command: ");
            String input = scanner.nextLine();
            CommandPair commandPair = parseCommand(input);
            doCommand(commandPair.command, commandPair.filename);
        }
    }

    /**
     * Executes the specified command with an optional filename argument.
     * @param command the command to execute
     * @param filename the file associated with the command, if applicable
     */
    private void doCommand(Command command, String filename) {
        switch (command) {
            case TRANSMIT:
                client.transmitFile(filename);
                break;
            case RECEIVE:
                client.receiveFile(filename);
                break;
            case HELP:
                System.out.println(menu());
                break;
            case EXIT:
                System.exit(0);
                break;
            default:
                System.err.println("Invalid command.");
                System.out.println(menu());
                break;
        }
    }

    /**
     * Parses a command line input into a command and a potential filename.
     * @param command the full command string input by the user
     * @return a CommandPair containing the parsed command and filename
     */
    private CommandPair parseCommand(String command) {
        String[] parts = command.split(" ");
        try {
            Command cmd = Command.valueOf(parts[0].toUpperCase().trim());
            String filename = (parts.length > 1) ? parts[1] : null;
            return new CommandPair(cmd, filename);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid command");
            return new CommandPair(Command.HELP);
        }
    }


    /**
     * Provides a help message with usage instructions for the program.
     * @return a formatted string detailing the program commands
     */
    public static String menu() {
        return """
                Usage:\s
                \ttransmit <filename> - (This would upload the file to the server)
                \treceive <filename> - (This would download the file from the server)
                \texit - Exits the program
                \thelp - Prints the Menu message again
                """;
    }

    /**
     * Inner class to hold a command and an optional filename.
     */
    private static class CommandPair {
        private final Command command; // The command to be executed
        private final String filename;// The associated filename, if applicable

        public CommandPair(Command cmd, String filename) {
            this.command = cmd;
            this.filename = filename;
        }
        // constructor
        public CommandPair(Command command) {
            this(command, null);
        }
    }


    /**
     * Enum to define the available commands that can be executed by this command line interface.
     */
    private enum Command {
        TRANSMIT, // Command to transmit (upload) a file to the server
        RECEIVE, // Command to receive (download) a file from the server
        HELP, // Command to display the help message
        EXIT // Command to exit the program
    }
}
