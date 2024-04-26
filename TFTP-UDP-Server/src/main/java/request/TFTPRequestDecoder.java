package request;

import exceptions.TFTPException;
import java.util.Arrays;

/**
 * This class is used to decode packets received from the TFTP server to ensure they are
 * correctly formatted and to extract information such as file names, block numbers, and data.
 * It handles different types of packets like ACK, WRQ, RRQ, and DATA.
 */
public class TFTPRequestDecoder {

	/**
	 * Represents a WRQ (Write Request) or RRQ (Read Request) packet.
	 */
	public static class WrqOrRrqPacket {
		public final String filename;
		public final OPCODE opcode;

		public WrqOrRrqPacket(String filename, OPCODE op) {
			this.filename = filename;
			this.opcode = op;
		}
	}

	/**
	 * Represents a DATA packet containing a block of file data.
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
	 * Decodes an ACK packet to extract the block number.
	 * @param packet The byte array containing the ACK packet.
	 * @return The block number.
	 * @throws TFTPException if the packet is invalid.
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
	 * Extracts the operation code from a packet.
	 * @param packet The byte array containing the packet.
	 * @return The operation code as an OPCODE enum.
	 * @throws TFTPException if the opcode is invalid.
	 */
	public static OPCODE decodeOp(byte[] packet) throws TFTPException {
		try {
			int operation = decodeUint16(packet, 0);
			return OPCODE.values()[operation];
		} catch (Exception e) {
			throw new TFTPException("Could not unpack opcode");
		}
	}

	/**
	 * Decodes a WRQ or RRQ packet to extract filename and mode.
	 * @param packet The byte array containing the packet.
	 * @param offset The starting index within the byte array.
	 * @return A WrqOrRrqPacket object with the filename and opcode.
	 * @throws TFTPException if the packet is invalid.
	 */
	public static WrqOrRrqPacket decodeWRQorRRQ(byte[] packet, int offset) throws TFTPException {
		try {
			int operation = decodeUint16(packet, offset);
			assert operation == OPCODE.WRQ.getValue() || operation == OPCODE.RRQ.getValue();

			offset += 2;
			String filename = decodeString(packet, offset);

			offset += filename.length() + 1;
			String mode = decodeString(packet, offset);
			assert mode.equals("octet");

			return new WrqOrRrqPacket(filename, OPCODE.values()[operation]);
		} catch (Exception e) {
			throw new TFTPException("Invalid WRQ/RRQ packet");
		}
	}

	/**
	 * Decodes a string from a byte array starting at a specified offset until a null byte is encountered.
	 * @param packet The byte array containing the string.
	 * @param offset The starting index for decoding.
	 * @return The decoded string.
	 * @throws TFTPException if the string cannot be decoded.
	 */
	private static String decodeString(byte[] packet, int offset) throws TFTPException {
		int i = offset;
		while (packet[i] != 0) {
			i++;
		}
		return new String(packet, offset, i - offset);
	}

	/**
	 * Decodes two bytes from a packet starting at a specified offset into a 16-bit integer.
	 * @param packet The byte array.
	 * @param offset The offset from which to start decoding.
	 * @return The decoded 16-bit integer.
	 */
	public static int decodeUint16(byte[] packet, int offset) {
		int high = packet[offset] & 0xFF;
		int low = packet[offset + 1] & 0xFF;
		return (high << 8) | low;
	}

	/**
	 * Decodes a DATA packet, extracting the block number and data.
	 * @param packet The byte array containing the DATA packet.
	 * @param offset The starting index within the byte array.
	 * @return A DataPacket object containing the block number and data.
	 * @throws TFTPException if the packet is invalid.
	 */
	public static DataPacket decodeData(byte[] packet, int offset) throws TFTPException {
		try {
			int op = decodeUint16(packet, offset);
			if (op != OPCODE.DATA.getValue()) {
				throw new TFTPException("Invalid DATA packet");
			}
			offset += 2;
			int block = decodeUint16(packet, offset);

			offset += 2;
			int end = offset;
			while (end < packet.length && packet[end] != 0) {
				end++;
			}
			byte[] data = Arrays.copyOfRange(packet, offset, end);
			return new DataPacket(block, data, data.length);
		} catch (Exception e) {
			throw new TFTPException("Invalid DATA packet");
		}
	}
}
