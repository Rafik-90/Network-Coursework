package request;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * This class is responsible for building and managing data packets for file transfers.
 * It handles the creation, storage, and writing of data packets to files under the constraints
 * of the TFTP protocol which limits file sizes to 32 MB.
 */
public class DataPacketsBuilder {
	private static final int MAX_BYTES_PER_FILE = 33554432; // 32 MB limit on file size in TFTP

	private byte[] data;// Buffer to store the file data
	private int size = 0; // Current size of data in the buffer
	private String filename; // Name of the file being written to


	/**
	 * Constructor initializes the data buffer with the maximum file size allowed by TFTP.
	 */
	public DataPacketsBuilder() {
		data = new byte[MAX_BYTES_PER_FILE];
	}


	/**
	 * Sets the filename for the file to be written.
	 * @param filename The name of the file.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
		System.out.println("Filename: " + filename);
	}


	/**
	 * Sets the data buffer to the specified data array and updates the size.
	 * @param data The data to set.
	 */
	public void setData(byte[] data) {
		this.data = data;
		this.size = data.length;
	}


	/**
	 * Adds a data packet to the current data buffer.
	 * @param dataPacket The data packet to add.
	 */
	public void addDataPacket(TFTPRequestDecoder.DataPacket dataPacket) {
		System.arraycopy(dataPacket.data, 0, data, size, dataPacket.size);
		size += dataPacket.size;
	}



	/**
	 * Returns the current data buffer.
	 * @return The data buffer.
	 */
	public byte[] getData() {
		return data;
	}


	/**
	 * Retrieves a byte of data at the specified index.
	 * @param index The index of the byte to retrieve.
	 * @return The byte at the specified index.
	 */
	public byte getData(int index) {
		return data[index];
	}



	/**
	 * Returns the current size of the data buffer.
	 * @return The size of the data.
	 */
	public int getSize() {
		return size;
	}


	/**
	 * Returns the filename of the file to be written.
	 * @return The filename.
	 */
	public String getFilename() {
		return filename;
	}


	/**
	 * Saves the current data buffer to a file at the path derived from the current filename.
	 * @throws IOException If an I/O error occurs.
	 */
	public void save() throws IOException {
		String path = new File(".").getCanonicalPath() + '/' + filename;

		// Create the file
		File file = new File(path);

		System.out.println("Saving file to: " + path);


		FileOutputStream fos = new FileOutputStream(file);

		fos.write(data, 0, size);
		fos.flush();
		fos.close();
		reset();

	}

	public void reset() {
		size = 0;
		filename = null;
		data = new byte[MAX_BYTES_PER_FILE];
	}


	/**
	 * Calculates the number of packets needed to send the entire file given a specific packet size.
	 * @param packetSize The size of each packet.
	 * @return The number of packets needed.
	 */
	public int getNumPackets(int packetSize) {
		int ret =  (int) Math.ceil((double) size / packetSize);
		return ret;

	}
}
