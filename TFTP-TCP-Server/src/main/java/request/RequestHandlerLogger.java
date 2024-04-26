package request;
import java.net.InetAddress;



/**
 * RequestHandlerLogger class provides logging functionality for a TFTP (Trivial File Transfer Protocol) server.
 */
public class RequestHandlerLogger {
    private static final String TAG = "TFTP_REQUEST_HANDLER";

    private final InetAddress clientAddress;
    private final int clientPort;
    private String fileName;

    /**
     * Constructor for RequestHandlerLogger.
     *
     * @param clientAddress the IP address of the client
     * @param clientPort    the port number of the client
     */
    public RequestHandlerLogger(InetAddress clientAddress, int clientPort) {
        this.clientAddress = clientAddress;
        this.clientPort  = clientPort;
    }


    /**
     * Returns a string representation of the client's address and port.
     *
     * @return string representation of the client's address and port
     */
    private String stringifyClientAddress() {
        return clientAddress.getHostAddress() + ":" + clientPort;
    }


    /**
     * Logs a message.
     *
     * @param msg     the message to log
     * @param isError true if the message is an error message, false otherwise
     */
    private void log(String msg, boolean isError) {
        String logMsg = TAG + " - " +  stringifyClientAddress() + ": " + msg;
        if (isError) {
            System.err.println(logMsg);
        } else {
            System.out.println(logMsg);
        }
    }


    /**
     * Logs a message.
     *
     * @param msg the message to log
     */
    private void log(String msg) {
        log(msg, false);
    }


    /**
     * Logs a received request (RRQ or WRQ).
     *
     * @param opcode   the opcode of the request (RRQ or WRQ)
     * @param filename the name of the file requested
     */
    private void logReceievedRequest(OPCODE opcode, String filename) {
        log(String.format("Received %s - Filename: %s", opcode, filename));
    }


    /**
     * Logs a received Read Request (RRQ) request.
     *
     * @param filename the name of the file requested
     */
    public void logRRQ(String filename) {
        logReceievedRequest(OPCODE.RRQ, filename);
    }


    /**
     * Logs a received Write Request (WRQ) request.
     *
     * @param filename the name of the file requested
     */
    public void logWRQ(String filename) {
        logReceievedRequest(OPCODE.WRQ, filename);
    }


    /**
     * Logs the receipt of data.
     *
     * @param filename    the name of the file
     * @param blockNumber the block number
     * @param dataLength  the length of the data received
     */
    public void logDATAReceived(String filename, int blockNumber, int dataLength) {
        log(String.format("Received DATA for %s - Block no %d of size %d bytes ", filename, blockNumber, dataLength));
    }


    /**
     * Logs the sending of data.
     *
     * @param filename     the name of the file
     * @param blockNumber  the block number
     * @param dataLength   the length of the data sent
     * @param totalBlocks  the total number of blocks
     */
    public void logDATASent(String filename, int blockNumber, int dataLength, int totalBlocks) {
        log(String.format("Sent DATA block %d/%d for %s - Block size: %d bytes", blockNumber,totalBlocks, filename, dataLength));
    }

    /**
     * Logs the end of the data transfer.
     *
     * @param filename    the name of the file
     * @param isReceiving true if the transfer was a written to the server, false otherwise
     */
    public void logDATAEnd(String filename, boolean isReceiving) {
        log(String.format("%s all data from %s",
                isReceiving ? "Received" : "Sent",
                filename));
    }


    /**
     * Logs the saving of a file.
     *
     * @param path the path where the file is saved
     */
    public void logFileSave(String path) {
        log(String.format("Saved file to %s", path));
    }


    /**
     * Logs an error message.
     *
     * @param errorMessage the error message to log
     */
    public void logError(String errorMessage) {
        log(errorMessage, true);
    }

}
