package org.hunters.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;


public class SocketAcceptor implements Runnable{

    private final int port;
    private final Queue<Socket> socketQueue;

    public SocketAcceptor(int tcpPort, Queue<Socket> socketQueue)  {
        this.port     = tcpPort;
        this.socketQueue = socketQueue;
    }

    @Override
    public void run() {
        ServerSocketChannel serverSocket = null;
        try{
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(port));
            System.out.println("Server listening on port " + port);
        } catch(IOException e){
            e.printStackTrace();
            return;
        }

        //noinspection InfiniteLoopStatement
        while(true){
            try{
                SocketChannel socketChannel = serverSocket.accept();

                System.out.println("Socket accepted: " + socketChannel);

                this.socketQueue.add(new Socket(socketChannel));

            } catch(IOException e){
                e.printStackTrace();
            }

        }

    }
}
