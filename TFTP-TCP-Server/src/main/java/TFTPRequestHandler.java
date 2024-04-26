import exceptions.TFTPException;
import request.*;
import java.io.*;
import java.net.Socket;



/**
 * TFTPRequestHandler class handles TCP TFTP requests sent by the client.
 */
public class TFTPRequestHandler extends Thread {

    private final RequestHandlerLogger logger;
    private final Socket clientSocket;
    private DataPacketsBuilder dataPacketsBuilder;

    private OutputStream out;
    private InputStream in;


    /**
     * Constructor for TFTPRequestHandler.
     *
     * @param clientSocket the client socket
     */
    public TFTPRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        logger = new RequestHandlerLogger(clientSocket.getInetAddress(), clientSocket.getPort());
        dataPacketsBuilder = new DataPacketsBuilder(logger);
    }


    /**
     * Runs the TFTPRequestHandler thread.
     */
    public void run() {
        try {
            out = clientSocket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            in = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] requestBuffer = new byte[1024];
        // read the request as bytes
        try {
            int read = in.read(requestBuffer);
        } catch (IOException e) {
            this.interrupt();
            if (this.isInterrupted()) {
                logger.logError("Client disconnected");
            }
        }

        // Handle the request.
        try {
            handleRequest(requestBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles the request.
     *
     * @param request the request buffer
     * @throws TFTPException if there is an error in the TFTP protocol
     */
    public void handleRequest(byte[] request) throws TFTPException {

        OPCODE opcode;
        opcode = TFTPRequestDecoder.decodeOp(request);
        assert opcode != null;
        // Handle the request
        try {
            switch (opcode) {
                case RRQ -> handleRRQ(request);
                case WRQ -> handleWRQ(request);
            }
        } catch (Exception e) {
            throw new TFTPException(e.getMessage());
        }
    }



    /**
     * Handles a Read Request (RRQ).
     *
     * @param request the request buffer
     * @throws Exception if an error occurs
     */
    private void handleRRQ(byte[] request) throws Exception {
        TFTPRequestDecoder.WrqOrRrqPacket req = TFTPRequestDecoder.unpackWRQorRRQ(request, 0);
        logger.logRRQ(req.filename);

        // Attempt to build the data packets from the file
        try {
            dataPacketsBuilder = DataPacketsBuilder.fromFile(req.filename, logger);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                logger.logError("File does not exist");
                byte[] buf = new byte[512];
                int len = TFTPRequestBuilder.createPackError(buf, 0x01, "File does not exist");
                out.write(buf, 0, len);
            } else {
                throw e;
            }
        }

        int numPackets = dataPacketsBuilder.getNumPackets(512);
        byte[] data = dataPacketsBuilder.getData();

        for (int i = 1; i <= numPackets; i++) {
            byte[] buffer = new byte[TFTPRequestBuilder.MAX_DATA_BYTES];
            // Get the current packet (leaving room for the opcode and block number - 4 bytes total)
            int start = (i - 1) * (TFTPRequestBuilder.MAX_DATA_BYTES - 4);
            int end = Math.min(start + TFTPRequestBuilder.MAX_DATA_BYTES - 4, data.length);
            byte[] dataPacket = new byte[end - start];
            System.arraycopy(data, start, dataPacket, 0, end - start);
            // Send the packet to the server.
            TFTPRequestBuilder.createPackData(buffer, i, dataPacket);
            logger.logDATASent(req.filename, i, end - start, numPackets);
            out.write(buffer);
        }
    }


    /**
     * Handles a Write Request (WRQ).
     *
     * @param request the request buffer
     * @throws Exception if an error occurs
     */
    private void handleWRQ(byte[] request) throws Exception {
        TFTPRequestDecoder.WrqOrRrqPacket req = TFTPRequestDecoder.unpackWRQorRRQ(request, 0);
        dataPacketsBuilder.reset();

        dataPacketsBuilder.setFilename(req.filename);
        logger.logWRQ(req.filename);

        // Wait for the client to send the first data packet
        int dataSize = TFTPRequestBuilder.MAX_DATA_BYTES - 4;

        while (dataSize >= TFTPRequestBuilder.MAX_DATA_BYTES - 4) {
            byte[] packet = new byte[TFTPRequestBuilder.MAX_DATA_BYTES];
            int read = in.read(packet);
            if (read == -1) {
                throw new TFTPException("Client has disconnected");
            }
            TFTPRequestDecoder.DataPacket dataPacket = TFTPRequestDecoder.decodeData(packet, 0);
            dataPacketsBuilder.addDataPacket(dataPacket);
            dataSize = dataPacket.data.length;
            logger.logDATAReceived(req.filename, dataPacket.blockNumber, dataPacket.size);
        }

        // Last Packet Received, Save the File.
        logger.logDATAEnd(req.filename, true);

        dataPacketsBuilder.save();

    }


    /**
     * Closes the input and output streams and the client socket.
     */
    private void closeStreams() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
