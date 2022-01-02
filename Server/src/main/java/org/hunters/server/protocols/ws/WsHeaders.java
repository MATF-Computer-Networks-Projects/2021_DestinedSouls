package org.hunters.server.protocols.ws;

import org.hunters.server.MessageMetaData;
import org.hunters.server.protocols.http.HttpHeaders;
import org.hunters.server.protocols.ws.framing.Frame;

public class WsHeaders extends MessageMetaData {
    public Frame frame      = null;
    public String key       = null;
    public String protocol  = null;

    public WsHeaders() {
    }
    public WsHeaders(HttpHeaders httpHeaders) {
        this.key = httpHeaders.ws.key;
        this.protocol = httpHeaders.ws.protocol;
    }
}
