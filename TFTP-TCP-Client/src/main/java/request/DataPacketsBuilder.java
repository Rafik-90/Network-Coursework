package request;

import Exceptions.TFTPException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Handles the creation and management of data packets from a file for TFTP operations,
 * and also provides functionality to write these packets back to a file.
 */
public class DataPacketsBuilder {
	// Sets a maximum file size limit of 32 MB for TFTP operations
	private static final int MAX_BYTES_PER_FILE = 33554432; // 32 MB limit on file size in TFTP

	private byte[] data;
	private int size = 0;
	private String filename;

	/**
	 * Constructor that initializes the data buffer to the maximum allowed size.
	 */
	public DataPacketsBuilder() {
		data = new byte[MAX_BYTES_PER_FILE];
	}


	/**
	 * Static method to create a DataPacketsBuilder from a file.
	 * @param filename The name of the file to be loaded into the builder.
	 * @return An instance of DataPacketsBuilder loaded with file data.
	 * @throws TFTPException If the file doesn't exist or exceeds size limits.
	 * @throws IOException If an I/O error occurs.
	 */
	public static DataPacketsBuilder fromFile(String filename) throws TFTPException, IOException {
		DataPacketsBuilder dataPacketsBuilder = new DataPacketsBuilder();
		dataPacketsBuilder.setFilename(filename);

		String path = new File(".").getCanonicalPath() + '/' + filename;
		// Create the file
		File file = new File(path);

		// Check if the file exists
		if (!file.exists()) {
			throw new TFTPException("File does not exist");
		}

		// Check if the file is too large
		if (file.length() > MAX_BYTES_PER_FILE) {
			throw new TFTPException("File is too large");
		}

		// Read the file into the data packets builder
		byte[] data = new byte[(int) file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(data);
		fis.close();
		dataPacketsBuilder.setData(data);
		return dataPacketsBuilder;
	}


	/**
	 * Sets the filename for the data packets.
	 * @param filename The filename to be set.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
		System.out.println("Filename: " + filename);
	}


	/**
	 * Sets the data buffer and updates the size based on the actual data length.
	 * @param data The data to set.
	 */
	public void setData(byte[] data) {
		this.data = data;
		this.size = data.length;
	}


	/**
	 * Appends a data packet to the current data buffer.
	 * @param dataPacket The data packet to add.
	 */
	public void addDataPacket(TFTPRequestDecoder.DataPacket dataPacket) {
		System.arraycopy(dataPacket.data, 0, data, size, dataPacket.size);
		size += dataPacket.size;
	}


	/**
	 * Returns the entire data buffer.
	 * @return The data buffer.
	 */
	public byte[] getData() {
		return data;
	}


	/**
	 * Gets a single byte of data at a specific index.
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
	 * Gets the filename associated with the data buffer.
	 * @return The filename.
	 */
	public String getFilename() {
		return filename;
	}



	/**
	 * Writes the data buffer to a file.
	 * @throws IOException If an I/O error occurs while writing.
	 */
	public void save() throws IOException {

		// Save the file into the resources folder of the project
		// Get the resources folder
		String path = new File(".").getCanonicalPath() + '/' + filename;
		// Create the file
		File file = new File(path);

		System.out.println("Saving file to: " + path);


		FileOutputStream fos = new FileOutputStream(file);

		fos.write(data, 0, size);

		fos.flush();
		reset();

	}


	/**
	 * Resets the builder to its initial state, clearing the data buffer and setting size to zero.
	 */
	public void reset() {
		size = 0;
		filename = null;
		data = new byte[MAX_BYTES_PER_FILE];
	}


	/**
	 * Calculates the number of packets needed to send the file based on a specified packet size.
	 * @param packetSize The size of each packet.
	 * @return The number of packets needed.
	 */
	public int getNumPackets(int packetSize) {
		return  (int) Math.ceil((double) size / packetSize);

	}
}
