package org.hunters.server;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;


public class Server {

    private SocketAccepter  socketAccepter  = null;
    private SocketProcessor socketProcessor = null;

    private int port = 0;
    private IMessageReaderFactory messageReaderFactory = null;
    private IMessageProcessor     messageProcessor = null;

    public Server(int port, IMessageReaderFactory messageReaderFactory, IMessageProcessor messageProcessor) {
        this.port = port;
        this.messageReaderFactory = messageReaderFactory;
        this.messageProcessor = messageProcessor;
    }

    public void start() throws IOException {

        Queue socketQueue = new ArrayBlockingQueue(1024);

        this.socketAccepter  = new SocketAccepter(port, socketQueue);


        MessageBuffer readBuffer  = new MessageBuffer();
        MessageBuffer writeBuffer = new MessageBuffer();

        this.socketProcessor = new SocketProcessor(socketQueue, readBuffer, writeBuffer,  this.messageReaderFactory, this.messageProcessor);

        Thread accepterThread  = new Thread(this.socketAccepter);
        Thread processorThread = new Thread(this.socketProcessor);

        accepterThread.start();
        processorThread.start();
    }


}
