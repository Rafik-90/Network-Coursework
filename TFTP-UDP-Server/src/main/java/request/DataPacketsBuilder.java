package request;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Builds up data packets for a given file and manages saving them to disk.
 * This class is used in the context of TFTP operations to handle large files by breaking
 * them into manageable data packets.
 */
public class DataPacketsBuilder {
    private static final int MAX_BYTES_PER_FILE = 33554432; // 32 MB limit on file size in TFTP

    private byte[] data; // Buffer to hold the file data
    private int size = 0; // Current size of the data in the buffer
    private String filename; // Filename for the saved file

    /**
     * Constructs a new DataPacketsBuilder instance initializing the data buffer.
     */
    public DataPacketsBuilder() {
        data = new byte[MAX_BYTES_PER_FILE];
    }

    /**
     * Sets the filename for the file to be saved.
     * @param filename The name of the file.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Sets the data for this builder, copying from an external array.
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
     * Returns the complete data buffer.
     * @return The data buffer as a byte array.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the byte value at a specific index in the data buffer.
     * @param index The index to retrieve the byte from.
     * @return The byte value at the specified index.
     */
    public byte getData(int index) {
        return data[index];
    }

    /**
     * Returns the current size of the data in the buffer.
     * @return The size of the data.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the filename of the file to be saved.
     * @return The filename.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Saves the data packets to a file on disk.
     * @throws IOException If an I/O error occurs writing to the file.
     */
    public void save() throws IOException {
        String path = new File(".").getCanonicalPath() + '/' + filename; // Determine the file's path
        File file = new File(path); // Create a new file object
        FileOutputStream fos = new FileOutputStream(file); // Create a file output stream
        fos.write(data, 0, size); // Write the data to file
        fos.flush(); // Flush the stream to ensure all data is written
        fos.close(); // Close the stream
        reset(); // Reset the builder for reuse
    }

    /**
     * Resets the data builder, clearing the data buffer and other properties.
     */
    public void reset() {
        size = 0;
        filename = null;
        data = new byte[MAX_BYTES_PER_FILE];
    }

    /**
     * Calculates the number of packets needed to send the file given the packet size.
     * @param packetSize The size of each packet.
     * @return The number of packets needed.
     */
    public int getNumPackets(int packetSize) {
        return (int) Math.ceil((double) size / packetSize);
    }
}
