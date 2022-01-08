package org.hunters.server.protocols;

public enum Protocol {
    HTTP(0),
    WS(1);

    private int value;
    Protocol(int value) {
        this.value = value;
    }
}
