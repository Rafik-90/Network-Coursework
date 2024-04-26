package request;



/**
 * The TFTPRequestBuilder class is responsible for creating the various types of packets
 * used in the Trivial File Transfer Protocol (TFTP), following the protocol's specifications.
 * It supports constructing read request (RRQ), write request (WRQ), data (DATA), and
 * acknowledgment (ACK) packets. The packets are built according to the TFTP standard,
 * which specifies the packet structure for each type of communication.
 */
public class TFTPRequestBuilder {
	public static int MAX_BYTES = 512; // Maximum data size for a single TFTP packet (512 bytes + headers)

	/**
	 * Enum representing TFTP operation codes (opcodes).
	 */
	public enum OPCODE {
		NOOP(0), RRQ(1), WRQ(2), DATA(3), ACK(4), ERROR(5);
		private final int value;
		OPCODE(int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
	}



	/**
	 * Builds a RRQ packet for requesting a file from the server.
	 * @param buf The buffer to fill with the packet data.
	 * @param filename The name of the file being requested.
	 * @return The length of the completed packet.
	 */
	public static int createPackRRQ(byte[] buf, String filename) {
		return createPackRRQorWRQ(buf, OPCODE.RRQ, filename);
	}


	/**
	 * Builds a WRQ packet for sending a file to the server.
	 * @param buf The buffer to fill with the packet data.
	 * @param filename The name of the file being sent.
	 * @return The length of the completed packet.
	 */
	public static int createPackWRQ(byte[] buf, String filename) {
		return createPackRRQorWRQ(buf, OPCODE.WRQ, filename);
	}


	/**
	 * Helper method to construct a RRQ or WRQ packet.
	 * The method uses the octet mode as the transfer mode according to the TFTP specifications.
	 * @param buf The buffer to fill with the packet data.
	 * @param op The opcode indicating whether it's a RRQ or WRQ.
	 * @param filename The name of the file being transferred.
	 * @return The length of the completed packet.
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
	 * Builds a DATA packet containing a block of file data.
	 * @param buf The buffer to fill with the packet data.
	 * @param block The block number of this data packet.
	 * @param data The data to include in the packet.
	 * @return The length of the completed packet.
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
	 * Builds an ACK packet acknowledging receipt of a data block.
	 * @param buf The buffer to fill with the packet data.
	 * @param block The block number being acknowledged.
	 * @return The length of the completed packet.
	 */
	public static int createPackAck(byte[] buf, int block) {
		int length = 0;
		length += createPackUInt16(buf, length, OPCODE.ACK.getValue());
		length += createPackUInt16(buf, length, block);
		length += createPackUInt16(buf, length, 0);
		return length;
	}

	/**
	 * Packs a 16-bit unsigned integer into a byte buffer.
	 * @param buf The buffer to which the integer will be packed.
	 * @param offset The offset in the buffer at which to start packing.
	 * @param value The integer value to pack into the buffer.
	 * @return The number of bytes consumed in the buffer (always 2 for a 16-bit integer).
	 */
	public static int createPackUInt16(byte[] buf, int offset, int value) {
		buf[offset] = (byte) (value >> 8);
		buf[offset + 1] = (byte) (value);
		return 2;
	}




	/**
	 * Appends a string to a byte buffer, converting the string to bytes.
	 * @param buf The buffer to which the string will be appended.
	 * @param offset The offset in the buffer at which to start appending.
	 * @param str The string to append to the buffer.
	 * @return The number of bytes consumed in the buffer, equivalent to the string's length.
	 */
	public static int createPackString(byte[] buf, int offset, String str) {
		byte[] bytes = str.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			buf[offset + i] = bytes[i];
		}
		return bytes.length;
	}
}
