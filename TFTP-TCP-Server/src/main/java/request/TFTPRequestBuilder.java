package request;


/**
 * TFTPRequestBuilder class is responsible for building various TFTP (Trivial File Transfer Protocol) request packets.
 */
public class TFTPRequestBuilder {

	/**
	 * Maximum number of bytes allowed in a TFTP packet.
	 */
	public static int MAX_DATA_BYTES = 516;


	/**
	 * Creates a Read Request (RRQ) packet.
	 *
	 * @param buf      the buffer to fill with the packet
	 * @param filename the name of the file to request
	 * @return the length of the created packet
	 */
	public static int createPackRRQ(byte[] buf, String filename) {
		return createPackRRQorWRQ(buf, OPCODE.RRQ, filename);
	}


	/**
	 * Creates a Write Request (WRQ) packet.
	 *
	 * @param buf      the buffer to fill with the packet
	 * @param filename the name of the file to request
	 * @return the length of the created packet
	 */
	public static int createPackWRQ(byte[] buf, String filename) {
		return createPackRRQorWRQ(buf, OPCODE.WRQ, filename);
	}


	/**
	 * Creates either a Read Request (RRQ) or Write Request (WRQ) packet.
	 *
	 * @param buf      the buffer to fill with the packet
	 * @param op       the type of operation (RRQ or WRQ)
	 * @param filename the name of the file to request
	 * @return the length of the created packet
	 */
	private static int createPackRRQorWRQ(byte[] buf, OPCODE op, String filename) {
		int length = 0;

		length += createPackUInt16(buf, length, op.getValue());

		length += createPackString(buf, length, filename);
		buf[length++] = 0;
		length += createPackString(buf, length, "octet");
		buf[length++] = 0;

		return length;
	}


	/**
	 * Creates an Error packet.
	 *
	 * @param buf  the buffer to fill with the packet
     * @param errorCode    the error code
     * @param errorMessage the error message
     * @return the length of the created packet
     */
	public static int createPackError(byte[] buf, int errorCode, String errorMessage) {
		int length = 0;

		length += createPackUInt16(buf, length, OPCODE.ERROR.getValue());
		length += createPackUInt16(buf, length, errorCode);
		length += createPackString(buf, length, errorMessage);
		buf[length++] = 0;

		return length;
	}



	/**
	 * Creates a Data packet.
	 *
	 * @param buf   the buffer to fill with the packet
	 * @param block the block number
	 * @param data  the data to be included in the packet
	 * @return the length of the created packet
	 */
	public static int createPackData(byte[] buf, int block, byte[] data) {
		// ensure that the data is not longer than the maximum packet size
		assert data.length <= MAX_DATA_BYTES;
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
	 *
	 * @param buf   the buffer to fill with the packet
	 * @param block the block number
	 * @return the length of the created packet
	 */
	public static int createPackAck(byte[] buf, int block) {
		int length = 0;
		length += createPackUInt16(buf, length, OPCODE.ACK.getValue());
		length += createPackUInt16(buf, length, block);
		length += createPackUInt16(buf, length, 0);
		return length;
	}

	/**
	 * Packs a 16-bit unsigned integer into a 2-byte array and appends it to the buffer.
	 *
	 * @param buf    the buffer to fill with the integer
	 * @param offset the offset at which to append the integer
	 * @param value  the integer value
	 * @return the number of bytes written (always 2)
	 */
	public static int createPackUInt16(byte[] buf, int offset, int value) {
		buf[offset] = (byte) (value >> 8);
		buf[offset + 1] = (byte) (value);
		return 2;
	}




	/**
	 * Appends a string to the buffer at the given offset.
	 *
	 * @param buf    the buffer to append the string to
	 * @param offset the offset at which to append the string
	 * @param str    the string to append
	 * @return the number of bytes written
	 */
	public static int createPackString(byte[] buf, int offset, String str) {
		byte[] bytes = str.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			buf[offset + i] = bytes[i];
		}
		return bytes.length;
	}
}
