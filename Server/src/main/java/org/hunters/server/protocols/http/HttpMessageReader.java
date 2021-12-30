package org.hunters.server.protocols.http;

import org.hunters.server.Message;
import org.hunters.server.MessageReader;
import org.hunters.server.Socket;
import org.hunters.server.protocols.ws.WsHeaders;
import org.hunters.server.protocols.ws.WsMessageReader;

import java.nio.ByteBuffer;


public class HttpMessageReader extends MessageReader {

    public HttpMessageReader() {
    }

    @Override
    protected void onLimitExceeded(Socket socket, ByteBuffer byteBuffer) {
        Message message = this.messageBuffer.getMessage();
        message.metaData = new HttpHeaders();
        ((HttpHeaders)this.nextMessage.metaData).httpMethod = EHttpMethod.ERROR;
        message.writePartialMessageToMessage(nextMessage, 0);
        completeMessages.add(nextMessage);
        nextMessage = message;
        byteBuffer.clear();
        socket.endOfStreamReached = true;
    }

    @Override
    protected void protocolSpecificParse(Socket socket, ByteBuffer byteBuffer) {
        int endIndex = HttpUtil.parseHttpRequest(this.nextMessage.sharedArray, this.nextMessage.offset,
                this.nextMessage.offset + this.nextMessage.length, (HttpHeaders) this.nextMessage.metaData);

        if( endIndex != -1 ) {
            Message message = this.messageBuffer.getMessage();
            message.metaData = new HttpHeaders();

            message.writePartialMessageToMessage(nextMessage, endIndex);
            completeMessages.add(nextMessage);

            if(((HttpHeaders)this.nextMessage.metaData).ws != null) {
                socket.messageReader = new WsMessageReader(this);
                message.offset = 0;
                message.length = 0;
                message.metaData = new WsHeaders((HttpHeaders)this.nextMessage.metaData);
            }

            nextMessage = message;
        }
        byteBuffer.clear();
    }
}
