package org.hunters.server.http;

import org.hunters.server.IMessageReader;
import org.hunters.server.IMessageReaderFactory;


public class HttpMessageReaderFactory implements IMessageReaderFactory {

    public HttpMessageReaderFactory() {
    }

    @Override
    public IMessageReader createMessageReader() {
        return new HttpMessageReader();
    }
}
