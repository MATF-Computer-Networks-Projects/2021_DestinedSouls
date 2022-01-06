package org.hunters.server;

import org.hunters.server.models.users.User;
import org.hunters.server.protocols.http.*;
import org.hunters.server.protocols.ws.WsHeaders;
import org.hunters.server.protocols.ws.WsMessageProcessor;
import org.hunters.server.routes.Router;
import org.hunters.server.services.StorageService;
import org.hunters.server.services.UserService;
import org.hunters.server.utils.FileInfo;
import org.hunters.server.utils.Response;
import org.hunters.server.utils.Responses;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class MessageProcessor {

    private final HttpMessageProcessor httpMessageProcessor = new HttpMessageProcessor();
    private final WsMessageProcessor wsMessageProcessor = new WsMessageProcessor();

    public MessageProcessor(Router router) {
        httpMessageProcessor.router = router;
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
