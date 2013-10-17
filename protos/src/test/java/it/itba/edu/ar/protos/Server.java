package it.itba.edu.ar.protos;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Properties;

/*
 * This is a simple Socket server that listens on a port and prints whatever it receives 
 * */

public class Server {

	private static Properties prop = new Properties();
	private static String propfilename = "proxy.properties";

	private static String port;
	private static String address;

	public static void main(String[] args) {

		getProperties();

		// Create socket
		ServerSocketChannel ssc = null;

		try {
			// Set address
			InetAddress addr = InetAddress.getByName(address);
			// Open socket
			ssc = ServerSocketChannel.open();
			// bind socket to port
			ssc.socket().bind(
					new InetSocketAddress(addr, Integer.valueOf(port)));
			// configure as non blocking
			ssc.configureBlocking(false);

			while (true) {
				// accept incoming connection
				SocketChannel sc = ssc.accept();
				// if sc == null, there is no connection yet
				if (sc == null) {
					// pretend to do something useful here
					System.out.println("Awaiting connection...");
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else { // received an incoming connection
					System.out.println("Incoming connection from "
							+ sc.socket().getRemoteSocketAddress());
					// Print info received through socket
					printRequest(sc);
					// Close socket
					sc.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ssc != null) {
				try {
					// Close ServerSocketChannel
					ssc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void printRequest(SocketChannel sc) throws IOException {
		ReadableByteChannel rbc = Channels.newChannel(sc.socket()
				.getInputStream());
		WritableByteChannel wbc = Channels.newChannel(System.out);
		ByteBuffer b = ByteBuffer.allocate(8); // read 8 bytes
		while (rbc.read(b) != -1) {
			b.flip();
			while (b.hasRemaining()) {
				wbc.write(b);
			}
			b.clear();
		}
	}

	private static void getProperties() {
		try {
			prop.load(new FileInputStream(propfilename));
			port = prop.getProperty("port");
			address = prop.getProperty("address");
		} catch (IOException e) {
			System.out.println("Unable to load " + propfilename + " file");
		}
	}
}
