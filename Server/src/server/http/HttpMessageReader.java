package server.http;

import server.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class HttpMessageReader implements IMessageReader {

    private MessageBuffer messageBuffer    = null;

    private List<Message> completeMessages = new ArrayList<Message>();
    private Message       nextMessage      = null;

    public HttpMessageReader() {
    }

    @Override
    public void init(MessageBuffer readMessageBuffer) {
        this.messageBuffer        = readMessageBuffer;
        this.nextMessage          = messageBuffer.getMessage();
        this.nextMessage.metaData = new HttpHeaders();
    }

    @Override
    public void read(Socket socket, ByteBuffer byteBuffer) throws IOException {
        int bytesRead = socket.read(byteBuffer);

        byteBuffer.flip();

        if(byteBuffer.remaining() == 0){
            byteBuffer.clear();
            return;
        }

        bytesRead = this.nextMessage.writeToMessage(byteBuffer);
        if(bytesRead == -1) {
            Message message = this.messageBuffer.getMessage();
            message.metaData = new HttpHeaders();
            ((HttpHeaders)this.nextMessage.metaData).httpMethod = EHttpMethod.ERROR;
            message.writePartialMessageToMessage(nextMessage, 0);
            completeMessages.add(nextMessage);
            nextMessage = message;
            byteBuffer.clear();
            socket.endOfStreamReached = true;
            return;
        }

        int endIndex = HttpUtil.parseHttpRequest(this.nextMessage.sharedArray, this.nextMessage.offset,
                                            this.nextMessage.offset + this.nextMessage.length,
                                                    (HttpHeaders) this.nextMessage.metaData);
        if(endIndex != -1) {
            Message message = this.messageBuffer.getMessage();
            message.metaData = new HttpHeaders();

            message.writePartialMessageToMessage(nextMessage, endIndex);

            completeMessages.add(nextMessage);
            nextMessage = message;
        }
        byteBuffer.clear();
    }


    @Override
    public List<Message> getMessages() {
        return this.completeMessages;
    }

}
