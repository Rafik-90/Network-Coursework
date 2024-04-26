package request;
import exceptions.TFTPException;
import java.util.Arrays;


// This class is responsible for decoding the different types of packets received as part of the TFTP protocol.
public class TFTPRequestDecoder {

	// Nested class representing a Write Request (WRQ) or Read Request (RRQ) packet.
	public static class WrqOrRrqPacket {
		public final String filename;// The filename to write/read
		public final OPCODE opcode;// The operation code (WRQ or RRQ)

		// Constructor for the WRQ/RRQ packet
		public WrqOrRrqPacket(String filename, OPCODE op) {
			this.filename = filename;
			this.opcode = op;
		}

	}

	// Nested class representing a Data packet.
	public static class DataPacket {
		public final int blockNumber;// The block number of this data packet
		public final int size;// The size of the data in this packet
		public final byte[] data;// The actual data

		// Constructor for the Data packet
		public DataPacket(int blockNumber, byte[] data, int size) {
			this.blockNumber = blockNumber;
			this.data = data;
			this.size = size;
		}
	}


	// Decodes an ACKnowledgment packet to retrieve the block number.
	public static int decodeACK(byte[] packet) throws TFTPException {
		try {
			int offset = 0;
			int operation = decodeUint16(packet, offset);
			assert operation == OPCODE.ACK.getValue();// Ensure that the operation code is ACK

			offset += 2;// Move past the opcode
			return decodeUint16(packet, offset);// Return the block number
		} catch (Exception e) {
			throw new TFTPException("Not valid ACK packet");
		}
	}

	// Decodes the first two bytes of a packet to retrieve the opcode.
	public static OPCODE decodeOp(byte[] packet) throws TFTPException {
		try {
			int operation = decodeUint16(packet, 0);
			return OPCODE.values()[operation];
		} catch (Exception e) {
			throw new TFTPException("Not able to unpack opcode");
		}
	}


	// Unpacks a WRQ or RRQ packet to retrieve the filename and opcode.
	public static WrqOrRrqPacket unpackWRQorRRQ(byte[] packet, int offset) throws TFTPException {
		try {
			// Check opcode
			int op = decodeUint16(packet, offset);// Get the opcode from the packet
			assert op == OPCODE.WRQ.getValue() || op == OPCODE.RRQ.getValue();// Ensure it's WRQ or RRQ

			// Check filename
			offset += 2;// Move past the opcode
			String filename = decodeString(packet, offset);// Decode the filename

			// Check mode
			offset += filename.length() + 1;// Move past the filename and null terminator
			String mode = decodeString(packet, offset);// Decode the mode (should be "octet")
			assert mode.equals("octet");// Ensure the mode is "octet"

			return new WrqOrRrqPacket(filename, OPCODE.values()[op]);// Return the decoded WRQ/RRQ packet
		} catch (Exception e) {
			throw new TFTPException("NOT valid WRQ/RRQ packet");
		}
	}

	// Decodes a string from the packet until a null terminator is encountered.
	private static String decodeString(byte[] packet, int offset) throws TFTPException {
		int i = offset;
		while (packet[i] != 0) {
			i++;
		}
		return new String(packet, offset, i - offset);
	}


	// Returns 2 bytes from the packet
	public static int decodeUint16(byte[] packet, int offset) {
		int high = packet[offset] & 0xFF;// High byte
		int low = packet[offset + 1] & 0xFF;// Low byte
		return (high << 8) | low;// Combine the bytes to form an integer
	}


	// Decodes a DATA packet to retrieve the block number and data
	public static DataPacket decodeData(byte[] packet, int offset) throws TFTPException {
		// Check opcode
		int operation = decodeUint16(packet, offset);
		if (operation != OPCODE.DATA.getValue()) {
			throw new TFTPException("NOT valid DATA packet - Incorrect opcode");
		}
		offset += 2;// Move past the opcode
		// Block number
		int block = decodeUint16(packet, offset);

		// Data
		offset += 2;// Move past the block number
		// Rest of packet contains data, find the end of the data
		int end = offset;
		while (end < packet.length && packet[end] != 0) {
			end++;// Find the end of the data
		}
		byte[] data = Arrays.copyOfRange(packet, offset, end); // Copy the data from the packet
		return new DataPacket(block, data, data.length);// Return the DataPacket instance


	}
}
