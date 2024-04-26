import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

/**
 * Server class that listens for incoming TFTP requests on a specific port and handles each request.
 * The server is designed to operate continuously, processing packets from multiple clients concurrently.
 */
public class Server {
    private static final int PORT = 8888; // Port number on which the server listens for incoming packets.

    // Map to track ongoing request handlers for each client based on their address and port.
    private static final HashMap<String, TFTPRequestHandler> clients = new HashMap<>();

    /**
     * Main method to start the server. It initializes a socket on a specified port and continuously
     * listens for incoming datagram packets. Each packet is processed by a TFTPRequestHandler.
     *
     * @param args Command line arguments (not used in this server).
     * @throws Exception if an I/O error occurs.
     */
    public static void main(String[] args) throws Exception {
        // Create a DatagramSocket to receive and respond to UDP packets
        DatagramSocket socket = new DatagramSocket(PORT);

        while (true) { // Run an infinite loop to keep the server running
            byte[] buffer = new byte[1024]; // Buffer to store incoming data
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Receive a packet (this method blocks until a packet is received)
            socket.receive(packet);

            // Construct a unique key for the client using its address and port
            String clientAddress = packet.getAddress().getHostAddress() + ":" + packet.getPort();

            // Check if the handler for this client already exists
            if (!clients.containsKey(clientAddress)) {
                System.out.println("New client: " + clientAddress);
                // If not, create a new TFTPRequestHandler and add it to the map
                clients.put(clientAddress, new TFTPRequestHandler(socket));
            }

            // Retrieve the handler for the current client and handle the received packet
            TFTPRequestHandler handler = clients.get(clientAddress);
            handler.handle(packet);
        }
    }
}
