package request;

import java.net.InetAddress;

/**
 * Logger for the TFTP Server, providing logging functionalities specifically designed
 * to capture and display actions and events related to TFTP operations.
 */
public class RequestHandlerLogger {
    private static final String TAG = "TFTP_REQUEST_HANDLER"; // Tag used for logging messages

    private final InetAddress clientAddress; // Client's IP address
    private final int clientPort; // Client's port number

    /**
     * Constructor to create a new logger instance for a specific client.
     *
     * @param clientAddress The IP address of the client.
     * @param clientPort The port number of the client.
     */
    public RequestHandlerLogger(InetAddress clientAddress, int clientPort) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    /**
     * Converts the client's IP address and port to a string format.
     *
     * @return A string representing the client's address and port.
     */
    private String stringifyClientAddress() {
        return clientAddress.getHostAddress() + ":" + clientPort;
    }

    /**
     * Logs a message to the console.
     *
     * @param msg The message to log.
     * @param isError Whether the message is an error.
     */
    private void log(String msg, boolean isError) {
        String logMsg = TAG + " - " + stringifyClientAddress() + ": " + msg;
        if (isError) {
            System.err.println(logMsg); // Log errors to the error stream
        } else {
            System.out.println(logMsg); // Log regular messages to the output stream
        }
    }

    /**
     * Logs a non-error message.
     *
     * @param msg The message to log.
     */
    private void log(String msg) {
        log(msg, false);
    }

    /**
     * Logs a received request, specifying the operation type and the filename involved.
     *
     * @param opcode The TFTP opcode indicating the type of request.
     * @param filename The filename involved in the request.
     */
    private void logReceievedRequest(OPCODE opcode, String filename) {
        log(String.format("Received %s - Filename: %s", opcode, filename));
    }

    /**
     * Logs a received Read Request (RRQ).
     *
     * @param filename The filename requested.
     */
    public void logRRQ(String filename) {
        logReceievedRequest(OPCODE.RRQ, filename);
    }

    /**
     * Logs a received Write Request (WRQ).
     *
     * @param filename The filename to be written.
     */
    public void logWRQ(String filename) {
        logReceievedRequest(OPCODE.WRQ, filename);
    }

    /**
     * Logs the reception of a data packet.
     *
     * @param filename The filename associated with the data packet.
     * @param blockNumber The block number of the data packet.
     * @param dataLength The size of the data packet in bytes.
     */
    public void logDATAReceived(String filename, int blockNumber, int dataLength) {
        log(String.format("Received DATA for %s - Block no %d of size %d bytes ", filename, blockNumber, dataLength));
    }

    /**
     * Logs the sending of a data packet.
     *
     * @param filename The filename associated with the data.
     * @param blockNumber The block number of the data being sent.
     * @param dataLength The size of the data packet in bytes.
     * @param totalBlocks The total number of data blocks sent.
     */
    public void logDATASent(String filename, int blockNumber, int dataLength, int totalBlocks) {
        log(String.format("Sent DATA block %d/%d for %s - Block size: %d bytes", blockNumber, totalBlocks, filename, dataLength));
    }

    /**
     * Logs the completion of a data transfer.
     *
     * @param filename The filename associated with the transfer.
     * @param isReceiving True if the transfer was receiving data, false if sending.
     */
    public void logDATAEnd(String filename, boolean isReceiving) {
        log(String.format("%s all data from %s", isReceiving ? "Received" : "Sent", filename));
    }

    /**
     * Logs the saving of a file.
     *
     * @param path The path where the file was saved.
     */
    public void logFileSave(String path) {
        log(String.format("Saved file to %s", path));
    }

    /**
     * Logs an error message.
     *
     * @param errorMessage The error message to log.
     */
    public void logError(String errorMessage) {
        log(errorMessage, true);
    }

    /**
     * Logs the sending or receiving of an ACK packet.
     *
     * @param blockNumber The block number acknowledged.
     * @param didReceive True if the ACK was received, false if sent.
     */
    public void logACK(int blockNumber, boolean didReceive) {
        log(String.format("%s ACK Block %d", didReceive ? "Received" : "Sent", blockNumber));
    }
}
