import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;



/**
 * Server class represents a TFTP server that listens for incoming client connections.
 */
public class Server {

    ServerSocket serverSocket;


    /**
     * Starts the TFTP server on the specified port.
     *
     * @param port the port on which the server will listen for incoming connections
     * @throws IOException if an I/O error occurs while starting the server
     */
    public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
        while (true) {
            Socket clientSocket = serverSocket.accept();

            Thread t = new TFTPRequestHandler(clientSocket);

            t.start();

        }
    }


    /**
     * Stops the server.
     */
    private void stopServer() {
        assert serverSocket != null;
        try {
            serverSocket.close();
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.out.println("Error stopping server");
            System.exit(1);
        }
    }


    /**
     * Entry point of the application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.startServer(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
