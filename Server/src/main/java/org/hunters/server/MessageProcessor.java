package org.hunters.server;

import org.hunters.server.protocols.http.*;
import org.hunters.server.protocols.ws.WsHeaders;
import org.hunters.server.protocols.ws.WsMessageProcessor;
import org.hunters.server.routes.Router;
import org.hunters.server.services.StorageService;

import java.io.IOException;

public class MessageProcessor {

    private final HttpMessageProcessor httpMessageProcessor = new HttpMessageProcessor();
    private final WsMessageProcessor wsMessageProcessor = new WsMessageProcessor();

    public MessageProcessor(Router router) {
        httpMessageProcessor.router = router;
        try {
            StorageService.resetLocalCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process(Message request, WriteProxy writeProxy) {
        if(request.metaData instanceof HttpHeaders) {
            httpMessageProcessor.process(request, writeProxy);
            return;
        }
        if(request.metaData instanceof WsHeaders) {
            wsMessageProcessor.process(request, writeProxy);
            return;
        }
        var response = writeProxy.getMessage();
        response.socketId = request.socketId;
        response.writeToMessage(StorageService.cache.get("204").duplicate());
        writeProxy.enqueue(response);
    }

}
