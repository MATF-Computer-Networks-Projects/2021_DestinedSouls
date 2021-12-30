package org.hunters.server;

import org.hunters.server.protocols.http.HttpHeaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public abstract class MessageReader {

    protected MessageBuffer messageBuffer    = null;
    protected List<Message> completeMessages = new ArrayList<Message>();
    protected Message       nextMessage      = null;

    protected MessageReader() {
    }

    protected MessageReader(MessageReader other) {
        this.messageBuffer     = other.messageBuffer;
        this.completeMessages  = other.completeMessages;
        this.nextMessage       = other.nextMessage;
    }

    public void init(MessageBuffer readMessageBuffer) {
        this.messageBuffer        = readMessageBuffer;
        this.nextMessage          = messageBuffer.getMessage();
        this.nextMessage.metaData = new HttpHeaders();
    }

    public void read(Socket socket, ByteBuffer byteBuffer) throws IOException {
        int bytesRead = socket.read(byteBuffer);

        byteBuffer.flip();

        if (byteBuffer.remaining() == 0) {
            byteBuffer.clear();
            return;
        }
        bytesRead = this.nextMessage.writeToMessage(byteBuffer);
        if(bytesRead == -1) {
            onLimitExceeded(socket, byteBuffer);
            return;
        }

        protocolSpecificParse(socket, byteBuffer);
    }

    public List<Message> getMessages() {
        return this.completeMessages;
    }

    /**
     * Optional handling response for oversize messages.
     * Intended to be overridden in subclass.
     *
     * @param socket Handled Socket.
     * @param byteBuffer The ByteBuffer containing the message data to write.
     */
    protected void onLimitExceeded(Socket socket, ByteBuffer byteBuffer) {
    }

    /**
     * Parsing metadata for specific protocol.
     *
     * @param socket Handled Socket.
     * @param byteBuffer The ByteBuffer containing the message data to write.
     */
    protected abstract void protocolSpecificParse(Socket socket, ByteBuffer byteBuffer);
}
