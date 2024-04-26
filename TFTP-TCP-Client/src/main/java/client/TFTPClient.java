package client;

import Exceptions.TFTPException;
import request.DataPacketsBuilder;
import request.OPCODE;
import request.TFTPRequestBuilder;
import request.TFTPRequestDecoder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;



/**
 * A TFTP client implementation that provides functionality for uploading and downloading files
 * using the Trivial File Transfer Protocol (TFTP).
 */
public class TFTPClient implements InterfaceClient {
	// Client socket for sending and receiving data
	private final Socket socket;
	private InputStream in;// Input stream to read data from the server
	private OutputStream out;// Output stream to send data to the server
	private DataPacketsBuilder dataPacketsBuilder;// Builder for creating and managing data packets



	/**
	 * Constructor that initializes the client with a given socket.
	 * Sets up input and output streams for data transmission.
	 * @param socket the socket connecting to the server
	 * @throws TFTPException if an I/O error occurs setting up the streams
	 */
	public TFTPClient(Socket socket) throws TFTPException {
		this.socket = socket;
		dataPacketsBuilder = new DataPacketsBuilder();
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			throw new TFTPException("Error while creating input/output streams for the client");
		}
	}


	/**
	 * Re-initializes the input and output streams.
	 */
	public void resetStreams(){
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			System.err.println("Error while creating input/output streams for the client");
		}
	}


	/**
	 * Transmits a file to the TFTP server.
	 * @param filename the name of the file to be transmitted
	 * @return true if the file was transmitted successfully, false otherwise
	 */
	@Override
	public boolean transmitFile(String filename) {
		dataPacketsBuilder.reset();

		try {
			// Attempt to load the file into the data packets builder
			try {
				dataPacketsBuilder = DataPacketsBuilder.fromFile(filename);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return false;
			}
			// Construct and send the Write Request (WRQ) packet
			byte[] wrqPacket = new byte[512];
			TFTPRequestBuilder.createPackWRQ(wrqPacket, filename);
			out.write(wrqPacket);
			int numPackets = dataPacketsBuilder.getNumPackets(TFTPRequestBuilder.MAX_BYTES - TFTPRequestBuilder.HEADER_SIZE);
			byte[] data = dataPacketsBuilder.getData();

			for (int i = 1; i <= numPackets; i++) {
				byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
				// Get the current packet (leaving room for the opcode and block number - 4 bytes total)
				int start = (i - 1) * (TFTPRequestBuilder.MAX_BYTES - 4);
				int end = Math.min(start + TFTPRequestBuilder.MAX_BYTES - 4, data.length);
				byte[] dataPacket = new byte[end - start];
				System.arraycopy(data, start, dataPacket, 0, end - start);
				// Send the packet to the server.
				TFTPRequestBuilder.createPackData(buffer, i, dataPacket);
				System.out.println("Sending packet " + i + " of " + numPackets);

				out.write(buffer);

			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}


	/**
	 * Receives a file from the TFTP server.
	 * @param filename the name of the file to be received
	 * @return true if the file was received successfully, false otherwise
	 */
	@Override
	public boolean receiveFile(String filename) {

		dataPacketsBuilder.reset();
		dataPacketsBuilder.setFilename(filename);

		System.out.println("Getting file: " + filename);

		// Send the RRQ packet.
		byte[] rrqPacket = new byte[512];
		TFTPRequestBuilder.createPackRRQ(rrqPacket, filename);
		try {
			out.write(rrqPacket);
		} catch (IOException e) {
			System.err.println("Error while sending RRQ packet");
		}
		byte[] packet = new byte[TFTPRequestBuilder.MAX_BYTES];
		try {
			int read = in.read(packet);
		} catch (IOException e) {
			System.err.println("Error while reading packet");
		}

		// Decode the operation code from the received packet
		OPCODE opcode = null;
		try {
			opcode = TFTPRequestDecoder.decodeOp(packet);
		} catch (TFTPException e) {
			System.err.println("Error while unpacking opcode");
			return false;
		}

		// Handle error packets immediately
		if (opcode == OPCODE.ERROR) {
			// Unpack the error code and message.
			TFTPRequestDecoder.ErrorPacket errorPacket = null;
			try {
				errorPacket = TFTPRequestDecoder.decodeError(packet, 0);
			} catch (TFTPException e) {
				System.err.println("Error while unpacking error code and message");
			}
			assert errorPacket != null;
			System.err.printf("%nError (%d): %s%n", errorPacket.errorCode, errorPacket.errorMessage);
			return false;
		}

		// Receive data packets and write to the builder
		while (true) {
			TFTPRequestDecoder.DataPacket dataPacket = null;
			try {
				dataPacket = TFTPRequestDecoder.decodeData(packet, 0);

			} catch (TFTPException e) {
				System.err.println("Error while unpacking data packet");
				return false;
			}
			dataPacketsBuilder.addDataPacket(dataPacket);
			System.out.printf(
					"Received DATA block %d of size %d bytes\n",
					dataPacket.blockNumber,
					dataPacket.size
			);
			// Check if the last packet is received (i.e., less than full packet size)
			if (dataPacket.size < TFTPRequestBuilder.MAX_BYTES - TFTPRequestBuilder.HEADER_SIZE) {
				break;
			}
			try {
				packet = new byte[TFTPRequestBuilder.MAX_BYTES];
				int read = in.read(packet);
			} catch (IOException e) {
				System.err.println("Error while reading packet");
				return false;
			}

		}
		// Save the received data to a file
		try {
			dataPacketsBuilder.save();
		} catch (IOException e) {
			System.err.println("Error while saving file");
			return false;
		}

		return true;
	}

}
