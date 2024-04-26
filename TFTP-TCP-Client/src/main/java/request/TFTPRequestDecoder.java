package request;
import request.*;
import java.util.Arrays;
import Exceptions.TFTPException;

/**
 * This class is responsible for decoding TFTP packets received from a server.
 * It ensures that server responses are correctly formatted and matches expected
 * packet types such as acknowledgment (ACK), write request (WRQ), read request (RRQ),
 * data transmission, and error packets.
 */
public class TFTPRequestDecoder {

	/**
	 * Nested class representing a WRQ (Write Request) or RRQ (Read Request) packet.
	 */

	public static class WrqOrRrqPacket {
		public final String filename; // The name of the file to be read or written.
		public final OPCODE opcode; // The operation code (WRQ or RRQ)

		public WrqOrRrqPacket(String filename, OPCODE op) {
			this.filename = filename;
			this.opcode = op;
		}

	}

	/**
	 * Nested class representing a DATA packet containing a portion of the file being transferred.
	 */
	public static class DataPacket {
		public final int blockNumber;
		public final int size;
		public final byte[] data;

		public DataPacket(int blockNumber, byte[] data, int size) {
			this.blockNumber = blockNumber;
			this.data = data;
			this.size = size;
		}
	}

	/**
	 * Nested class representing an ERROR packet.
	 */
	public static class ErrorPacket {
		public final int errorCode; // The error code indicating the type of error.
		public final String errorMessage; // The human-readable error message

		public ErrorPacket(int errorCode, String errorMessage) {
			this.errorCode = errorCode;
			this.errorMessage = errorMessage;
		}
	}

	/**
	 * Decodes an ACK packet to retrieve the block number being acknowledged.
	 * @param packet The ACK packet as a byte array.
	 * @return The block number being acknowledged.
	 * @throws TFTPException If the packet is not a valid ACK packet.
	 */
	public static int decodeACK(byte[] packet) throws TFTPException {
		try {
			int offset = 0;
			int operation = decodeUint16(packet, offset);
			assert operation == OPCODE.ACK.getValue();

			offset += 2;
			return decodeUint16(packet, offset);
		} catch (Exception e) {
			throw new TFTPException("Invalid ACK packet");
		}
	}

	/**
	 * Extracts the opcode from a TFTP packet.
	 * @param packet The TFTP packet as a byte array.
	 * @return The opcode as defined in the TFTPRequestBuilder.OPCODE enum.
	 * @throws TFTPException If the opcode could not be unpacked.
	 */
	public static OPCODE decodeOp(byte[] packet) throws TFTPException {
		try {
			int operation = decodeUint16(packet, 0);
			return OPCODE.values()[operation];
		} catch (Exception e) {
			throw new TFTPException("Not able to unpack opcode");
		}
	}

	/**
	 * Decodes a WRQ or RRQ packet to obtain the filename and opcode.
	 * @param packet The packet as a byte array.
	 * @param offset The starting offset in the packet for decoding.
	 * @return A WrqOrRrqPacket object containing the filename and opcode.
	 * @throws TFTPException If the packet is not a valid WRQ/RRQ packet.
	 */
	public static WrqOrRrqPacket decodeWRQorRRQ(byte[] packet, int offset) throws TFTPException {
		try {
			// Check opcode
			int op = decodeUint16(packet, offset);
			assert op == OPCODE.WRQ.getValue() || op == OPCODE.RRQ.getValue();

			// Check filename
			offset += 2;
			String filename = decodeString(packet, offset);

			// Check mode
			offset += filename.length() + 1;
			String mode = decodeString(packet, offset);
			assert mode.equals("octet");

			return new WrqOrRrqPacket(filename, OPCODE.values()[op]);
		} catch (Exception e) {
			throw new TFTPException("Not valid WRQ/RRQ packet");
		}
	}

	/**
	 * Extracts a string from a packet starting at the given offset until a null byte is encountered.
	 * @param packet The packet as a byte array.
	 * @param offset The offset in the packet to start decoding.
	 * @return The extracted string.
	 * @throws TFTPException If the string could not be unpacked.
	 */
	private static String decodeString(byte[] packet, int offset) throws TFTPException {
		int i = offset;
		while (packet[i] != 0) {
			i++;
		}
		return new String(packet, offset, i - offset);
	}


	/**
	 * Extracts a 16-bit unsigned integer from the packet starting at the given offset.
	 * @param packet The packet as a byte array.
	 * @param offset The offset in the packet to start decoding.
	 * @return The extracted integer value.
	 */
	public static int decodeUint16(byte[] packet, int offset) {
		int high = packet[offset] & 0xFF;
		int low = packet[offset + 1] & 0xFF;
		return (high << 8) | low;
	}


	/**
	 * Decodes a DATA packet to extract the block number and the data bytes.
     * @param packet The packet as a byte array.
	 * @param offset The starting offset in the packet for decoding.
     * @return A DataPacket object containing the block number and data bytes.
	 * @throws TFTPException If the packet is not a valid DATA packet.
	 */
	public static DataPacket decodeData(byte[] packet, int offset) throws TFTPException {


		try {
			// Check opcode
			int operation = decodeUint16(packet, offset);
			if (operation != OPCODE.DATA.getValue()) {
				throw new TFTPException("Not valid DATA packet");
			}
			offset += 2;
			// Block number
			int blockNumber = decodeUint16(packet, offset);
		//	System.out.println("Block number: " + blockNumber);

			// Data
			offset += 2;
			// Rest of packet contains data, find the end of the data
			int end = offset;
			while (end < packet.length && packet[end] != 0) {
				end++;
			}
			byte[] data = Arrays.copyOfRange(packet, offset, end);
			return new DataPacket(blockNumber, data, data.length);
		} catch (Exception e) {
			throw new TFTPException("Not valid DATA packet");
		}
	}


	/**
	 * Decodes an ERROR packet to extract the error code and message.
	 * @param packet The packet as a byte array.
	 * @param offset The starting offset in the packet for decoding.
	 * @return An ErrorPacket object containing the error code and message.
	 * @throws TFTPException If the packet is not a valid ERROR packet.
	 */
	public static ErrorPacket decodeError(byte[] packet, int offset) throws TFTPException {
		try {
			// Check opcode
			int operation = decodeUint16(packet, offset);
			if (operation != OPCODE.ERROR.getValue()) {
				throw new TFTPException("Invalid ERROR packet");
			}
			offset += 2;
			// Error code
			int errorCode = decodeUint16(packet, offset);
			offset += 2;
			// Error message
			String errorMessage = decodeString(packet, offset);
			return new ErrorPacket(errorCode, errorMessage);
		} catch (Exception e) {
			throw new TFTPException("Invalid ERROR packet");
		}
	}
}
