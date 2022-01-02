package org.hunters.server.protocols.ws.framing;

import java.nio.ByteBuffer;

public abstract class Frame {
    public boolean    FIN;
    public boolean    RSV1;
    public boolean    RSV2;
    public boolean    RSV3;
    public Opcode     opcode;
    public long       length;
    public ByteBuffer payload;

    private final static byte mask = (byte)0x80;

    public Frame(Opcode opcode) {
        this.FIN    = true;
        this.RSV1   = false;
        this.RSV2   = false;
        this.RSV3   = false;
        this.opcode = opcode;
    }

    public void setPayload(ByteBuffer payload) {
        this.payload = payload;
    }

    public byte encoded() {
        int mask = this.FIN ? 128 : 0;
        mask |= this.RSV1 ? 64 : 0;
        mask |= this.RSV2 ? 32 : 0;
        mask |= this.RSV3 ? 16 : 0;
        mask |= opcode.toInt();

        return (byte) mask;
    }

    public void append(Frame nextframe) {
        ByteBuffer b = nextframe.payload;
        if (payload == null) {
            payload = ByteBuffer.allocate(b.remaining());
            b.mark();
            payload.put(b);
            b.reset();
        } else {
            b.mark();
            payload.position(payload.limit());
            payload.limit(payload.capacity());

            if (b.remaining() > payload.remaining()) {
                ByteBuffer tmp = ByteBuffer.allocate(b.remaining() + payload.capacity());
                payload.flip();
                tmp.put(payload);
                tmp.put(b);
                payload = tmp;

            } else {
                payload.put(b);
            }
            payload.rewind();
            b.reset();
        }
        FIN = nextframe.FIN;

    }

    /**
     * Get a frame with a specific opcode
     *
     * @param opcode the opcode representing the frame
     * @return the frame with a specific opcode
     */
    public static Frame get(Opcode opcode) {
        if (opcode == null)
            return null;
        switch (opcode) {
            case PING:
                return new PingFrame();
            case PONG:
                return new PongFrame();
            case TEXT:
                return new TextFrame();
            case BINARY:
                return new BinaryFrame();
            case CLOSING:
                return new CloseFrame();
            case CONTINUOUS:
                return new ContinuousFrame();
            default:
                return null;
        }
    }

    public static Frame getFromFirst(byte firstByte) {
        var f = get(Opcode.valueOf((byte) (firstByte & 0xf)).get());
        f.FIN    = (firstByte & mask) != 0; firstByte <<= 1;
        f.RSV1   = (firstByte & mask) != 0; firstByte <<= 1;
        f.RSV2   = (firstByte & mask) != 0; firstByte <<= 1;
        f.RSV3   = (firstByte & mask) != 0;
        return f;
    }

    public static Frame getFromBuffer(ByteBuffer byteBuffer) {
        var frame = getFromFirst(byteBuffer.get());


        frame.length = byteBuffer.get() & 0x7f;
        if(frame.length == 126)
            frame.length = byteBuffer.getShort();
        else if(frame.length == 127) {
            frame.length = byteBuffer.getLong();
        }
        frame.setPayload(byteBuffer);
        return frame;


    }
}
