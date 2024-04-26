package client;
import Exceptions.TFTPException;


/**
 * Interface defining the operations for a TFTP client that interacts with a server
 * to send and receive files using the TFTP protocol, specifically in octet mode,
 * which deals with binary file transfers in byte format.
 */
public interface InterfaceClient {
	/**
	 * Sends a file to a TFTP server using the specified filename.
	 * The filename may be a relative or absolute path to the file to be sent.
	 *
	 * @param filename The name or path of the file to send.
	 * @return true if the file is transmitted successfully, false otherwise.
	 */
	 boolean transmitFile(String filename);




	/**
	 * Receives a file from a TFTP server.
	 * The method should handle the specifics of TFTP protocol to ensure reliable transfer,
	 * including sending the necessary acknowledgments as specified by the protocol.
	 *
	 * @param filename The name of the file to receive.
	 * @return true if the file is received successfully, false otherwise.
	 * @throws TFTPException If there is an error during the file reception.
	 */
	 boolean receiveFile(String filename) throws TFTPException;
}
