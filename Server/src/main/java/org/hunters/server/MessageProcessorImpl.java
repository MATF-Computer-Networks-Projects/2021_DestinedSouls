package org.hunters.server;

import org.hunters.server.protocols.http.EHttpMethod;
import org.hunters.server.protocols.http.HttpHeaders;
import org.hunters.server.protocols.http.HttpRequest;
import org.hunters.server.protocols.http.HttpUtil;
import org.hunters.server.protocols.ws.WsHeaders;
import org.hunters.server.routes.Router;
import org.hunters.server.services.StorageService;
import org.hunters.server.utils.FileInfo;
import org.hunters.server.utils.Json;
import org.hunters.server.utils.Response;
import org.hunters.server.utils.Responses;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MessageProcessorImpl implements MessageProcessor {

    private final Router router;

    public MessageProcessorImpl(Router router) {
        this.router = router;
    }

    private boolean isAllowed(EHttpMethod method) { return method != EHttpMethod.ERROR; }

    @Override
    public void process(Message request, WriteProxy writeProxy) {

        Message response = writeProxy.getMessage();
        response.socketId = request.socketId;

        if(request.metaData instanceof HttpHeaders)
            processHttp(request, response);

        else if(request.metaData instanceof WsHeaders)
            processWs(request, response);
        else
            response.writeToMessage(StorageService.cache.get("204").duplicate());

        writeProxy.enqueue(response);
    }

    private void processHttp(Message request, Message response) {
        HttpHeaders headers = (HttpHeaders) request.metaData;

        if(!isAllowed(headers.httpMethod)) {
            response.writeToMessage(StorageService.cache.get("405"));
            return;
        }

        System.out.println("Url: " + headers.url);

        String filename = headers.url.substring(headers.url.lastIndexOf('/')+1);
        if(StorageService.cache.contains(filename)) {
            response.writeToMessage(StorageService.cache.get(filename).duplicate());
            return;
        }

        var httpRequest = new HttpRequest();
        httpRequest.headers = headers;

        if(headers.bodyStartIndex < headers.bodyEndIndex) {
            HttpUtil.resolvePayload(request, httpRequest);
        }

        response.writeToMessage( respond(router.forward(httpRequest) ));
    }

    private ByteBuffer respond(Response response) {
        if(response.status == 200) {
            if(response.json.hasKey("filename"))
                return StorageService.cache.get(response.json.get("filename")).duplicate();
            else if(response.json.hasKey("jsonArray"))
                return Responses.createResponseBuffer( FileInfo.json(response.json.get("jsonArray").getBytes()) );
            else if(response.json.hasKey("matches")) {
                byte[] matches = (",\"matches\":" + response.json.remove("matches") + "}").getBytes();
                String resStr = response.json.toString();
                int len = resStr.length()-1;
                byte[] resJson = resStr.substring(0,len).getBytes();
                ByteBuffer bf = ByteBuffer.allocate(resJson.length + matches.length);
                bf.put(resJson);
                bf.put(matches);
                bf.flip();
                return Responses.createResponseBuffer( FileInfo.json(bf) );
            }
            return Responses.createResponseBuffer( FileInfo.json(response.json.toString().getBytes()) );
        }

        if(response.status == 101) {
            return Responses.createSwitchingProtocols( response.json.get("key") );
        }

        // errors
        if(response.status == 400)
            return Responses.createBadRequest( response.json.hasKey("msg") ? response.json.get("msg") : "Bad request" );

        return StorageService.cache.get(String.valueOf(response.status)).duplicate();
    }


    private void processWs(Message request, Message response) {
        var frame = ((WsHeaders)request.metaData).frame;

        ByteBuffer bf = ByteBuffer.allocate(3);
        bf.put((byte)0);
        bf.put(frame.encoded());
        bf.put((byte)0);
        bf.flip();

        response.writeToMessage(bf);
    }
}
