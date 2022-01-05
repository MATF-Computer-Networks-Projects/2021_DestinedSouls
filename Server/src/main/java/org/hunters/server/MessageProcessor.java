package org.hunters.server;

import org.hunters.server.protocols.http.EHttpMethod;
import org.hunters.server.protocols.http.HttpHeaders;
import org.hunters.server.protocols.http.HttpRequest;
import org.hunters.server.protocols.http.HttpUtil;
import org.hunters.server.protocols.ws.WsHeaders;
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

    private final Router router;

    public MessageProcessor(Router router) {
        this.router = router;
    }

    private boolean isAllowed(EHttpMethod method) { return method != EHttpMethod.ERROR; }

    public void process(Message request, WriteProxy writeProxy) {

        Message response = writeProxy.getMessage();
        response.socketId = request.socketId;

        if(request.metaData instanceof HttpHeaders)
            processHttp(request, response);

        else if(request.metaData instanceof WsHeaders)
            processWs(request, response, writeProxy);
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
        if(headers.url.startsWith("/chat")) {
            headers.token = headers.url.split("token=",2)[1];
            filename = "chat";
            headers.url = "/chat";
            headers.ws.socketId = request.socketId;
        }
        else if(StorageService.cache.contains(filename)) {
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

    private String appendSocketId(Message request, int endIdx) {
        if(request.sharedArray[endIdx-1] != '}')
            return null;


        StringBuilder sb = new StringBuilder(new String(Arrays.copyOfRange(request.sharedArray,
                                                                           request.offset, endIdx)));
        sb.setCharAt(sb.length()-1, ',');
        sb.append("\"socketId\":\"")
          .append(request.socketId)
          .append("\"}");
        return sb.toString();
    }

    private void processWs(Message request, Message response, WriteProxy writeProxy) {
        var frame = ((WsHeaders)request.metaData).frame;


        String req = appendSocketId(request, (int) (request.offset+frame.length));
        if(req == null) {
            response.writeToMessage(StorageService.cache.get("1003").duplicate());
            return;
        }


        var res = this.router.forward("/chat", req);

        if(res.status != 0) {
            response.writeToMessage(StorageService.cache.get("1003").duplicate());
            return;
        }

        response.writeToMessage(Responses.wsEmptyJson(frame.encoded()));

        // int targetUserId   = Integer.parseInt(res.json.get("id"));
        int targetSocketId = Integer.parseInt(res.json.get("socketId"));
        if(targetSocketId != -1) {
            res.json.remove("socketId");
            Message wsMessage = writeProxy.getMessage();
            wsMessage.socketId = targetSocketId;
            wsMessage.writeToMessage( Responses.wsResponse(frame.encoded(),  res.json.toString() ) );
            writeProxy.enqueue(wsMessage);
        }

    }
}
