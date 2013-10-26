package it.itba.edu.ar.protos.proxy;
import it.itba.edu.ar.protos.Interfaces.TCPProtocol;
import it.itba.edu.ar.protos.handler.TCPRequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class ProxyServer {

    private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Parameter(s): <Port> ...");
        }
        String port = args[0];
        
        Selector selector = Selector.open();
        ServerSocketChannel listnChannel = ServerSocketChannel.open();
        listnChannel.socket().bind(new InetSocketAddress(Integer.parseInt(port)));
        listnChannel.configureBlocking(false); 
        listnChannel.register(selector, SelectionKey.OP_ACCEPT);
        TCPProtocol protocol = new TCPRequestHandler();
        
        while (true) { 
            if (selector.select(TIMEOUT) == 0) {
                System.out.print(".");
                continue;
            }
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while (keyIter.hasNext()) {
                SelectionKey key = keyIter.next();
                if (key.isAcceptable()) {
                    protocol.handleAccept(key);
                }
                if (key.isReadable()) {
                    protocol.handleRead(key);
                }
                if (key.isValid() && key.isWritable()) {
                    protocol.handleWrite(key);
                }
                keyIter.remove();
            }
        }
    }
}
