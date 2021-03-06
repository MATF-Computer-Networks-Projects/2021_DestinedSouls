package org.hunters.server;

import org.hunters.server.services.UserService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;


// public class SocketProcessor implements Runnable {
public class SocketProcessor {

    private final Queue<Socket>  inboundSocketQueue;

    private final MessageBuffer  readMessageBuffer;
    private final MessageBuffer  writeMessageBuffer;

    private final MessageReaderFactory messageReaderFactory;

    private final Queue<Message> outboundMessageQueue = new LinkedList<>();

    private final Map<Long, Socket> socketMap         = new HashMap<>();

    private final ByteBuffer readByteBuffer  = ByteBuffer.allocate(1024 * 1024);
    private final ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024 * 1024);
    private final Selector   readSelector;
    private final Selector   writeSelector;

    private final MessageProcessor  messageProcessor;
    private final WriteProxy        writeProxy;

    private       long              nextSocketId = 16 * 1024; //start incoming socket ids from 16K - reserve bottom ids for pre-defined sockets (servers).

    private final Set<Socket> emptyToNonEmptySockets = new HashSet<>();
    private final Set<Socket> nonEmptyToEmptySockets = new HashSet<>();


    public SocketProcessor(Queue<Socket> inboundSocketQueue, MessageBuffer readMessageBuffer, MessageBuffer writeMessageBuffer, MessageReaderFactory messageReaderFactory, MessageProcessor messageProcessor) throws IOException {
        this.inboundSocketQueue   = inboundSocketQueue;

        this.readMessageBuffer    = readMessageBuffer;
        this.writeMessageBuffer   = writeMessageBuffer;
        this.writeProxy           = new WriteProxy(writeMessageBuffer, this.outboundMessageQueue);

        this.messageReaderFactory = messageReaderFactory;

        this.messageProcessor     = messageProcessor;

        this.readSelector         = Selector.open();
        this.writeSelector        = Selector.open();
    }

    public void run() {
        //noinspection InfiniteLoopStatement
        while(true){
            try {
                executeCycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void executeCycle() throws IOException {
        takeNewSockets();
        readFromSockets();
        writeToSockets();
    }


    public void takeNewSockets() throws IOException {
        Socket newSocket = this.inboundSocketQueue.poll();

        while(newSocket != null){
            newSocket.socketId = this.nextSocketId++;
            newSocket.socketChannel.configureBlocking(false);

            newSocket.messageReader = this.messageReaderFactory.createMessageReader();
            newSocket.messageReader.init(this.readMessageBuffer);

            newSocket.messageWriter = new MessageWriter();

            this.socketMap.put(newSocket.socketId, newSocket);

            SelectionKey key = newSocket.socketChannel.register(this.readSelector, SelectionKey.OP_READ);
            key.attach(newSocket);

            newSocket = this.inboundSocketQueue.poll();
        }
    }


    public void readFromSockets() throws IOException {
        int readReady = this.readSelector.selectNow();

        if(readReady > 0){
            Set<SelectionKey> selectedKeys = this.readSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                readFromSocket(key);

                keyIterator.remove();
            }
            selectedKeys.clear();
        }

    }

    private void readFromSocket(SelectionKey key) throws IOException {
        Socket socket = (Socket) key.attachment();
        socket.messageReader.read(socket, this.readByteBuffer);

        List<Message> fullMessages = socket.messageReader.getMessages();
        if(fullMessages.size() > 0){
            for(Message message : fullMessages){
                message.socketId = socket.socketId;
                this.messageProcessor.process(message, this.writeProxy);
            }
            fullMessages.clear();
        }

        if(socket.endOfStreamReached){
            System.out.println("Socket closed: " + socket.socketId);
            this.socketMap.remove(socket.socketId);
            key.attach(null);
            key.cancel();
            key.channel().close();
        }
    }


    public void writeToSockets() throws IOException {

        // Take all new messages from outboundMessageQueue
        takeNewOutboundMessages();

        // Cancel all sockets which have no more data to write.
        cancelEmptySockets();

        // Register all sockets that *have* data and which are not yet registered.
        registerNonEmptySockets();

        // Select from the Selector.
        int writeReady = this.writeSelector.selectNow();

        if(writeReady > 0){
            Set<SelectionKey>      selectionKeys = this.writeSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator   = selectionKeys.iterator();

            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                Socket socket = (Socket) key.attachment();

                socket.messageWriter.write(socket, this.writeByteBuffer);

                if(socket.messageWriter.isEmpty()) {
                    this.nonEmptyToEmptySockets.add(socket);
                }
                keyIterator.remove();
            }
            selectionKeys.clear();
        }
    }

    private void registerNonEmptySockets() throws ClosedChannelException {
        for(Socket socket : emptyToNonEmptySockets){
            socket.socketChannel.register(this.writeSelector, SelectionKey.OP_WRITE, socket);
        }
        emptyToNonEmptySockets.clear();
    }

    private void cancelEmptySockets() {
        for(Socket socket : nonEmptyToEmptySockets){
            SelectionKey key = socket.socketChannel.keyFor(this.writeSelector);

            key.cancel();
        }
        for(var id : UserService.socketsToClose) {
            if(this.socketMap.containsKey(id))
                this.socketMap.get(id)
                        .socketChannel
                        .keyFor(this.writeSelector)
                        .cancel();
        }
        nonEmptyToEmptySockets.clear();
    }

    private void takeNewOutboundMessages() {
        Message outMessage = this.outboundMessageQueue.poll();
        while(outMessage != null){
            Socket socket = this.socketMap.get(outMessage.socketId);

            if(socket != null){
                MessageWriter messageWriter = socket.messageWriter;
                if(messageWriter.isEmpty()){
                    messageWriter.enqueue(outMessage);
                    nonEmptyToEmptySockets.remove(socket);
                    emptyToNonEmptySockets.add(socket);
                } else{
                   messageWriter.enqueue(outMessage);
                }
            }

            outMessage = this.outboundMessageQueue.poll();
        }
    }




}
