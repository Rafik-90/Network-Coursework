package request;


public class TFTPRequestBuilder {
	// Maximum bytes in a TFTP packet
	public static int MAX_BYTES = 516;
	// Standard size of the TFTP header
	public static int HEADER_SIZE = 4;



	/**
	 * Creates a Read Request (RRQ) packet.
	 * @param buf the buffer where the packet data will be stored
	 * @param filename the name of the file to read
	 * @return the length of the created packet
	 */
	public static int createPackRRQ(byte[] buf, String filename) {
		return createPackRRQorWRQ(buf, OPCODE.RRQ, filename);
	}

	/**
	 * Creates a Write Request (WRQ) packet.
	 * @param buf the buffer where the packet data will be stored
	 * @param filename the name of the file to write
	 * @return the length of the created packet
	 */
	public static int createPackWRQ(byte[] buf, String filename) {
		return createPackRRQorWRQ(buf, OPCODE.WRQ, filename);
	}




	/**
	 * Creates either a Read Request (RRQ) or Write Request (WRQ) packet.
	 * @param buf the buffer to store the packet data
	 * @param op the operation code (RRQ or WRQ)
	 * @param filename the name of the file to be transferred
	 * @return the length of the packet
	 */
	private static int createPackRRQorWRQ(byte[] buf, OPCODE op, String filename) {

		int length = 0;
		length += createPackUInt16(buf, length, op.getValue());
		length += createPackString(buf, length, filename);
		buf[length++] = 0;// Null terminator for the filename
		length += createPackString(buf, length, "octet");
		buf[length++] = 0;// Null terminator for the transfer mode

		return length;
	}

	/**
	 * Creates an Error packet.
	 * @param buf the buffer to store the packet data
	 * @param errorCode the error code
	 * @param errorMessage the error message
	 * @return the length of the packet
	 */
	public static int createPackError(byte[] buf, int errorCode, String errorMessage) {
		int length = 0;

		length += createPackUInt16(buf, length, OPCODE.ERROR.getValue());
		length += createPackUInt16(buf, length, errorCode);
		length += createPackString(buf, length, errorMessage);
		buf[length++] = 0; // Null terminator for the error message


		return length;
	}


	/**
	 * Creates a Data packet.
	 * @param buf the buffer to store the packet data
	 * @param block the block number
	 * @param data the data bytes to be transferred
	 * @return the length of the packet
	 */
	public static int createPackData(byte[] buf, int block, byte[] data) {
		// ensure that the data is not longer than the maximum packet size
		assert data.length <= MAX_BYTES;
		int length = 0;
		length += createPackUInt16(buf, length, OPCODE.DATA.getValue());

		// block number is 2 bytes
		// append the block number to the buffer
		length += createPackUInt16(buf, length, block);

		// Get the data from the file and append it to the buffer

		System.arraycopy(data, 0, buf, length, data.length);
		length += data.length;


		return length;
	}


	/**
	 * Creates an Acknowledgment (ACK) packet.
	 * @param buf the buffer to store the packet data
	 * @param block the block number to acknowledge
	 * @return the length of the packet
	 */
	public static int createPackAck(byte[] buf, int block) {
		int length = 0;
		length += createPackUInt16(buf, length, OPCODE.ACK.getValue());
		length += createPackUInt16(buf, length, block);
		length += createPackUInt16(buf, length, 0);
		return length;
	}


	/**
	 * Packs a 16-bit unsigned integer into the buffer at the specified offset.
	 * @param buf the buffer where the integer is packed
	 * @param offset the offset at which to start packing
	 * @param value the value to pack
	 * @return the number of bytes used (always 2)
	 */
	public static int createPackUInt16(byte[] buf, int offset, int value) {
		buf[offset] = (byte) (value >> 8); // Low byte
		buf[offset + 1] = (byte) (value); // High byte
		return 2;
	}




	/**
	 * Packs a string into the buffer at the specified offset.
	 * @param buf the buffer to store the string
	 * @param offset the offset at which to start packing
	 * @param str the string to pack
	 * @return the number of bytes used
	 */
	public static int createPackString(byte[] buf, int offset, String str) {
		byte[] bytes = str.getBytes();// Convert string to bytes
		for (int i = 0; i < bytes.length; i++) {
			buf[offset + i] = bytes[i];// Copy each byte to the buffer
		}
		return bytes.length;// Return the length of the string in bytes
	}
}
