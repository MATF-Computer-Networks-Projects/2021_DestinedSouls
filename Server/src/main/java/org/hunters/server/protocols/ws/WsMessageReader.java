package org.hunters.server.protocols.ws;

import org.hunters.server.Message;
import org.hunters.server.MessageReader;
import org.hunters.server.Socket;
import org.hunters.server.protocols.ws.framing.Frame;

import java.nio.ByteBuffer;

public class WsMessageReader extends MessageReader {
    public WsMessageReader() {
    }

    public WsMessageReader(MessageReader other) {
        super(other);
    }

    private final byte[] key = new byte[4];

    @Override
    protected void protocolSpecificParse(Socket socket, ByteBuffer byteBuffer) {

        this.nextMessage.metaData = new WsHeaders();
        var headers = (WsHeaders)this.nextMessage.metaData;

        byteBuffer.clear();
        headers.frame = Frame.getFromBuffer(byteBuffer);

        int len = (int)headers.frame.length;
        int off = this.nextMessage.offset;

        if(len > 4) {
            headers.frame.payload.get(key, 0, 4);
            headers.frame.payload.get(this.nextMessage.sharedArray, off, len);

            for (int i = 0; i < len; ++i)
                this.nextMessage.sharedArray[off + i] = (byte) (this.nextMessage.sharedArray[off + i] ^ key[i & 0x3]);
        }

        Message message = this.messageBuffer.getMessage();
        message.metaData = new WsHeaders();
        message.writePartialMessageToMessage(nextMessage, nextMessage.offset + nextMessage.length);

        completeMessages.add(nextMessage);
        nextMessage = message;

        byteBuffer.clear();
    }

}
