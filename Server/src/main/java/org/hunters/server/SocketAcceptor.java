package org.hunters.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;


public class SocketAcceptor implements Runnable{

    private int port = 0;
    private ServerSocketChannel serverSocket = null;

    private Queue<Socket> socketQueue = null;

    public SocketAcceptor(int tcpPort, Queue<Socket> socketQueue)  {
        this.port     = tcpPort;
        this.socketQueue = socketQueue;
    }

    @Override
    public void run() {
        try{
            this.serverSocket = ServerSocketChannel.open();
            this.serverSocket.bind(new InetSocketAddress(port));
        } catch(IOException e){
            e.printStackTrace();
            return;
        }

        //noinspection InfiniteLoopStatement
        while(true){
            try{
                SocketChannel socketChannel = this.serverSocket.accept();

                System.out.println("Socket accepted: " + socketChannel);

                this.socketQueue.add(new Socket(socketChannel));

            } catch(IOException e){
                e.printStackTrace();
            }

        }

    }
}
