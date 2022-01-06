package org.hunters.server;

import org.hunters.server.protocols.Protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class Socket {

    public long socketId;
    public Protocol protocol = Protocol.HTTP;

    public SocketChannel  socketChannel = null;
    public MessageReader  messageReader = null;
    public MessageWriter  messageWriter = null;

    public boolean endOfStreamReached = false;

    public Socket() {
    }

    public Socket(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public int read(ByteBuffer byteBuffer) throws IOException {
        int bytesRead = this.socketChannel.read(byteBuffer);
        int totalBytesRead = bytesRead;

        while(bytesRead > 0){
            bytesRead = this.socketChannel.read(byteBuffer);
            totalBytesRead += bytesRead;
        }
        if(bytesRead == -1){
            this.endOfStreamReached = true;
        }

        return totalBytesRead;
    }

    public int write(ByteBuffer byteBuffer) throws IOException{
        int bytesWritten      = this.socketChannel.write(byteBuffer);
        int totalBytesWritten = bytesWritten;

        while(bytesWritten > 0 && byteBuffer.hasRemaining()){
            bytesWritten = this.socketChannel.write(byteBuffer);
            totalBytesWritten += bytesWritten;
        }

        return totalBytesWritten;
    }


}
