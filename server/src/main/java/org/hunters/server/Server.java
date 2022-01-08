package org.hunters.server;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;


public class Server {

    private SocketAcceptor socketAcceptor = null;
    private SocketProcessor socketProcessor = null;

    private int port = 0;
    private MessageReaderFactory messageReaderFactory = null;
    private MessageProcessor messageProcessor = null;

    public Server(int port, MessageReaderFactory messageReaderFactory, MessageProcessor messageProcessor) {
        this.port = port;
        this.messageReaderFactory = messageReaderFactory;
        this.messageProcessor = messageProcessor;
    }

    public void start() throws IOException {

        Queue socketQueue = new ArrayBlockingQueue(1024);

        this.socketAcceptor = new SocketAcceptor(port, socketQueue);


        MessageBuffer readBuffer  = new MessageBuffer();
        MessageBuffer writeBuffer = new MessageBuffer();

        this.socketProcessor = new SocketProcessor(socketQueue, readBuffer, writeBuffer,  this.messageReaderFactory, this.messageProcessor);

        Thread accepterThread  = new Thread(this.socketAcceptor);

        accepterThread.start();

        this.socketProcessor.run();
    }


}
