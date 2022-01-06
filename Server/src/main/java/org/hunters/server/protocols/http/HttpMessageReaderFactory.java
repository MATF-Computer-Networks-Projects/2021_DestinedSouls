package org.hunters.server.protocols.http;

import org.hunters.server.MessageReader;
import org.hunters.server.MessageReaderFactory;


public class HttpMessageReaderFactory implements MessageReaderFactory {

    public HttpMessageReaderFactory() {
    }

    @Override
    public MessageReader createMessageReader() {
        return new HttpMessageReader();
    }
}
