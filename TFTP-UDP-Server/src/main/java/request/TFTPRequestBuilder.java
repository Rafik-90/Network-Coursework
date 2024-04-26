package request;

/**
 * The TFTPRequestBuilder class is responsible for creating various types of TFTP packets,
 * including RRQ, WRQ, DATA, ACK, and ERROR packets, specifically using octet mode.
 */
public class TFTPRequestBuilder {
	// Maximum size for data packets as defined by the TFTP protocol
	public static int MAX_BYTES = 512;

	/**
	 * Creates a Read Request (RRQ) packet.
	 * @param buf The buffer to store the packet data.
	 * @param filename The filename to request from the server.
	 * @return The total length of the created RRQ packet.
	 */
	public static int createPackRRQ(byte[] buf, String filename) {
		return createPackRRQorWRQ(buf, OPCODE.RRQ, filename);
	}

	/**
	 * Creates a Write Request (WRQ) packet.
	 * @param buf The buffer to store the packet data.
	 * @param filename The filename to write to the server.
	 * @return The total length of the created WRQ packet.
	 */
	public static int createPackWRQ(byte[] buf, String filename) {
		return createPackRRQorWRQ(buf, OPCODE.WRQ, filename);
	}

	/**
	 * Internal helper method to create either an RRQ or WRQ packet.
	 * @param buf The buffer to store the packet data.
	 * @param op The opcode indicating whether this is an RRQ or WRQ.
	 * @param filename The filename involved in the request.
	 * @return The total length of the created packet.
	 */
	private static int createPackRRQorWRQ(byte[] buf, OPCODE op, String filename) {
		int length = 0;
		length += createPackUInt16(buf, length, op.getValue()); // Pack the opcode
		length += createPackString(buf, length, filename); // Pack the filename
		buf[length++] = 0; // Null terminator for the string
		length += createPackString(buf, length, "octet"); // Pack the mode
		buf[length++] = 0; // Null terminator for the mode
		return length;
	}

	/**
	 * Creates an ERROR packet.
	 * @param buf The buffer to store the packet data.
	 * @param errorCode The error code.
	 * @param errorMessage The error message.
	 * @return The total length of the created ERROR packet.
	 */
	public static int createPackError(byte[] buf, int errorCode, String errorMessage) {
		int length = 0;
		length += createPackUInt16(buf, length, OPCODE.ERROR.getValue()); // Pack the ERROR opcode
		length += createPackUInt16(buf, length, errorCode); // Pack the error code
		length += createPackString(buf, length, errorMessage); // Pack the error message
		buf[length++] = 0; // Null terminator for the string
		return length;
	}

	/**
	 * Creates a DATA packet.
	 * @param buf The buffer to store the packet data.
	 * @param block The block number of the data.
	 * @param data The actual data bytes.
	 * @return The total length of the created DATA packet.
	 */
	public static int createPackData(byte[] buf, int block, byte[] data) {
		assert data.length <= MAX_BYTES; // Ensure data does not exceed max bytes
		int length = 0;
		length += createPackUInt16(buf, length, OPCODE.DATA.getValue()); // Pack the DATA opcode
		length += createPackUInt16(buf, length, block); // Pack the block number
		System.arraycopy(data, 0, buf, length, data.length); // Copy the actual data
		length += data.length;
		return length;
	}

	/**
	 * Creates an ACK packet.
	 * @param buf The buffer to store the packet data.
	 * @param block The block number being acknowledged.
	 * @return The total length of the created ACK packet.
	 */
	public static int createPackAck(byte[] buf, int block) {
		int length = 0;
		length += createPackUInt16(buf, length, OPCODE.ACK.getValue()); // Pack the ACK opcode
		length += createPackUInt16(buf, length, block); // Pack the block number
		length += createPackUInt16(buf, length, 0); // Zero padding for alignment
		return length;
	}

	/**
	 * Packs a 16-bit integer into the buffer at the specified offset.
	 * @param buf The buffer where the integer is to be packed.
	 * @param offset The offset within the buffer where packing starts.
	 * @param value The integer value to pack.
	 * @return The number of bytes used in the buffer.
	 */
	public static int createPackUInt16(byte[] buf, int offset, int value) {
		buf[offset] = (byte) (value >> 8); // High byte
		buf[offset + 1] = (byte) (value); // Low byte
		return 2;
	}

	/**
	 * Packs a string into the buffer at the specified offset.
	 * @param buf The buffer where the string is to be packed.
	 * @param offset The offset within the buffer where packing starts.
	 * @param str The string to pack.
	 * @return The number of bytes used in the buffer.
	 */
	public static int createPackString(byte[] buf, int offset, String str) {
		byte[] bytes = str.getBytes(); // Convert string to bytes
		for (int i = 0; i < bytes.length; i++) {
			buf[offset + i] = bytes[i]; // Pack each byte into the buffer
		}
		return bytes.length;
	}
}
