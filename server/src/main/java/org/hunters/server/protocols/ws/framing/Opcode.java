package org.hunters.server.protocols.ws.framing;

import java.util.Arrays;
import java.util.Optional;

public enum Opcode {
    CONTINUOUS(0x0),
    TEXT(0x1),
    BINARY(0x2),
    CLOSING(0x8),
    PING(0x9),
    PONG(0xA);

    private final int value;

    Opcode(int value) {
        this.value = value;
    }

    public static Optional<Opcode> valueOf(byte value) {
        return Arrays.stream(values())
                .filter(opcode -> opcode.value == value)
                .findFirst();
    }

    public int toInt() {
        return this.value;
    }
}
