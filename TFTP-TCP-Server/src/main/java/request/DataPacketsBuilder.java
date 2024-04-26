package request;
import exceptions.TFTPException;
import java.io.*;



/**
 * DataPacketsBuilder class is responsible for building data packets for a given file and saving them to a file.
 */
public class DataPacketsBuilder {
    private static final int MAX_BYTES_PER_FILE = 33554432; // 32 MB limit on file size in TFTP

    private byte[] data;
    private int size = 0;
    private String filename;

    private RequestHandlerLogger logger;


    /**
     * Constructor for DataPacketsBuilder.
     *
     * @param logger the logger to use for logging
     */
    public DataPacketsBuilder(RequestHandlerLogger logger) {
        data = new byte[MAX_BYTES_PER_FILE];
        this.logger = logger;
    }


    /**
     * Creates a DataPacketsBuilder instance from a file.
     *
     * @param filename the name of the file
     * @param logger   the logger to use for logging
     * @return a DataPacketsBuilder instance
     * @throws TFTPException if there is an error in the TFTP protocol
     * @throws IOException   if an I/O error occurs
     */
    public static DataPacketsBuilder fromFile(String filename, RequestHandlerLogger logger) throws TFTPException, IOException {
        DataPacketsBuilder dataPacketsBuilder = new DataPacketsBuilder(logger);
        dataPacketsBuilder.setFilename(filename);

        String path = new File(".").getCanonicalPath() + '/' + filename;
        // Create the file
        File file = new File(path);

        // Check if the file exists
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist");
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
     * Sets the filename.
     *
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
        System.out.println("Filename: " + filename);
    }


    /**
     * Sets the data.
     *
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
        this.size = data.length;
    }


    /**
     * Adds a data packet to the builder.
     *
     * @param dataPacket the data packet to add
     */
    public void addDataPacket(TFTPRequestDecoder.DataPacket dataPacket) {
        System.arraycopy(dataPacket.data, 0, data, size, dataPacket.size);
        size += dataPacket.size;
    }


    /**
     * Gets the data.
     *
     * @return the data
     */
    public byte[] getData() {
        return data;
    }


    /**
     * Gets the data at a specified index.
     *
     * @param index the index
     * @return the data at the specified index
     */
    public byte getData(int index) {
        return data[index];
    }


    /**
     * Gets the size of the data.
     *
     * @return the size of the data
     */
    public int getSize() {
        return size;
    }


    /**
     * Gets the filename.
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }



    /**
     * Saves the data packets to a file.
     *
     * @throws IOException if an I/O error occurs
     */
    public void save() throws IOException {

        // Save the file into the resources folder of the project
        // Get the resources folder
        String path = new File(".").getCanonicalPath() + '/' + filename;

        // Create the file
        File file = new File(path);

        logger.logFileSave(path);

        FileOutputStream fos = new FileOutputStream(file);

        fos.write(data, 0, size);

        fos.flush();
        reset();

    }

    /**
     * Resets the builder.
     */
    public void reset() {
        size = 0;
        filename = null;
        data = new byte[MAX_BYTES_PER_FILE];
    }


    /**
     * Calculates the number of data packets needed to send the file given a packet size in bytes.
     *
     * @param packetSize the packet size in bytes
     * @return the number of data packets needed
     */
    public int getNumPackets(int packetSize) {
        return (int) Math.ceil((double) size / packetSize);

    }
}
