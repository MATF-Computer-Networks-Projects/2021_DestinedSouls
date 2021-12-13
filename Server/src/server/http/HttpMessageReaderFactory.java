package server.http;

import server.IMessageReader;
import server.IMessageReaderFactory;


public class HttpMessageReaderFactory implements IMessageReaderFactory {

    public HttpMessageReaderFactory() {
    }

    @Override
    public IMessageReader createMessageReader() {
        return new HttpMessageReader();
    }
}
