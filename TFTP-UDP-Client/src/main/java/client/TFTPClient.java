package client;

import Exceptions.TFTPException;
import request.*;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;



/**
 * The TFTPClient class provides methods to send and receive files using the TFTP protocol.
 */
public class TFTPClient implements InterfaceClient {

	private InetAddress host;// Host IP address for the TFTP server
	private int port;// Port number on which the TFTP server is listening to
	private DatagramSocket socket;// Socket to send and receive datagram packets


	/**
	 * Constructor to create an instance of TFTPClient.
	 *
	 * @param ip   the IP address of the TFTP server as a String
	 * @param port the port number of the TFTP server
	 */
	public TFTPClient(String ip, int port) {
		try {
			this.host = InetAddress.getByName(ip);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error getting host: " + ip);
		}
		this.port = port;

		try {
			socket = new DatagramSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Sends a file to the TFTP server following the protocol's workflow:
	 * send a write request (WRQ), receive an acknowledgment (ACK), send the data, and wait for ACK for each data packet.
	 *
	 * @param filename the name of the file to be sent
	 * @return true if the file is sent successfully, false otherwise
	 */

	@Override
	public boolean transmitFile(String filename) {
		// Read file as byte array
		byte[] file = null;
		try {
			// Read the filename from the resources folder
			String path = new java.io.File(".").getCanonicalPath() + '/' + filename;
			System.out.println(path);
			file = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			System.out.println("No file found: " + filename);
			return false;
		}
		byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
		// Build WRQ packet
		int wrqReqSize = TFTPRequestBuilder.createPackWRQ(buffer, filename);
		DatagramPacket wrqPacket = new DatagramPacket(buffer, wrqReqSize, host, port);
		// Send WRQ packet
		try {
			socket.send(wrqPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error sending WRQ packet");
			return false;
		}
		// Wait till we receive ACK
		// clear buffer
		// Handle ACK for WRQ
		buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

		boolean hasReceivedACK = false;
		int numRetries = 0;

		try {
			socket.setSoTimeout(2000);
		} catch (SocketException e) {
			System.err.println("Error setting socket timeout");
			return false;
		}
		DatagramPacket ackPacket = new DatagramPacket(buffer, TFTPRequestBuilder.MAX_BYTES, host, port);
		while (!hasReceivedACK && numRetries < 3) {
			try {
				socket.receive(ackPacket);
			} catch (IOException e) {
				System.err.println("Error receiving ACK packet. Retrying...");
				numRetries++;
				continue;// Ignore and retry
			}
			try {
				int n = TFTPRequestDecoder.decodeACK(ackPacket.getData());
				hasReceivedACK = true;
			} catch (TFTPException e) {
				continue;
			}
			System.out.println("Received ACK, sending data...");
		}
		// Split file into packets
		// Send data packets
		int numPackets = (int) Math.ceil((double) file.length / (TFTPRequestBuilder.MAX_BYTES - 4));

		for (int i = 1; i <= numPackets; i++) {
			// clear buffer
			buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

			// Get the current packet (leaving room for the opcode and block number - 4 bytes total)
			int start = (i - 1) * (TFTPRequestBuilder.MAX_BYTES - 4);
			int end = Math.min(start + TFTPRequestBuilder.MAX_BYTES - 4, file.length);

			byte[] packet = new byte[end - start];

			System.arraycopy(file, start, packet, 0, end - start);
			// Build data packet by splitting file into 512 byte chunks
			int dataReqSize = TFTPRequestBuilder.createPackData(buffer, i, packet);
			DatagramPacket dataPacket = new DatagramPacket(buffer, dataReqSize, host, port);
			// Send DATA packet
			try {
				socket.send(dataPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error sending DATA packet");
				System.exit(1);
			}
			// Wait till we receive ACK
			try {
				socket.receive(ackPacket);

				// Ensure the ACK packet echos the block number we sent
				assert TFTPRequestDecoder.decodeACK(ackPacket.getData()) == i;
			} catch (Exception e) {
				if (e instanceof TFTPException) {
					e.printStackTrace();
				}
				System.err.println("Timed out waiting for ACK.\n");
				return false;
			}
			System.out.println("Sent packet " + i);
		}
		System.out.println("File sent successfully");
		return true;
	}


	/**
	 * Receives a file from the TFTP server by sending a read request (RRQ), waiting for the data packets,
	 * and sending ACK for each received packet until all packets are received.
	 *
	 * @param filename the name of the file to be received
	 * @return true if the file is received and saved successfully, false otherwise
	 */
	@Override
	public boolean receiveFile(String filename) {
		DataPacketsBuilder dataPacketsBuilder = new DataPacketsBuilder();
		byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
		int size = TFTPRequestBuilder.createPackRRQ(buffer, filename);
		DatagramPacket rrqPacket = new DatagramPacket(buffer, size, host, port);

		try {
			socket.send(rrqPacket);
			socket.setSoTimeout(2000);
		} catch (IOException e) {
			System.err.println("Error sending RRQ packet or setting socket timeout: " + e.getMessage());
			return false;
		}

		int expectedBlockNumber = 1;
		boolean lastPacket = false;

		while (!lastPacket) {
			buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
			DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, host, port);

			try {
				socket.receive(dataPacket);
				TFTPRequestDecoder.DataPacket packet = TFTPRequestDecoder.decodeData(dataPacket.getData(), 0);
				if (packet.blockNumber != expectedBlockNumber) {
					continue; // Skip to the next loop iteration if unexpected block number
				}
				dataPacketsBuilder.addDataPacket(packet);
				// Send ACK
				buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
				size = TFTPRequestBuilder.createPackAck(buffer, packet.blockNumber);
				DatagramPacket ackPacket = new DatagramPacket(buffer, size, host, port);
				socket.send(ackPacket);

				if (packet.size < TFTPRequestBuilder.MAX_BYTES - 4) {
					lastPacket = true; // Last packet of data
				} else {
					expectedBlockNumber++; // Increment block number for next expected packet
				}
			} catch (SocketTimeoutException e) {
				System.err.println("Socket timeout: " + e.getMessage());
				continue; // On timeout, continue to next loop iteration and resend ACK
			} catch (IOException e) {
				System.err.println("IO error: " + e.getMessage());
				return false; // On IOException, exit with failure
			} catch (TFTPException e) {
				System.err.println("TFTP error: " + e.getMessage());
				return false; // On TFTPException, exit with failure
			}
		}
		// Save the file to disk
		dataPacketsBuilder.setFilename(filename);
		try {
			dataPacketsBuilder.save();
			return true; // File saved successfully
		} catch (IOException e) {
			System.err.println("Error saving file: " + e.getMessage());
			return false; // Error occurred while saving the file
		}
	}

}
