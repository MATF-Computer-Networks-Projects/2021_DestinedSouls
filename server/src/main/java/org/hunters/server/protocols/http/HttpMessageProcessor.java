package org.hunters.server.protocols.http;

import org.hunters.server.Message;
import org.hunters.server.MessageProcessor;
import org.hunters.server.WriteProxy;
import org.hunters.server.models.users.User;
import org.hunters.server.protocols.ws.WsHeaders;
import org.hunters.server.routes.ChatController;
import org.hunters.server.routes.Router;
import org.hunters.server.services.StorageService;
import org.hunters.server.services.UserService;
import org.hunters.server.utils.FileInfo;
import org.hunters.server.utils.Json;
import org.hunters.server.utils.Response;
import org.hunters.server.utils.Responses;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpMessageProcessor {

    public Router router;

    private boolean isAllowed(EHttpMethod method) { return method != EHttpMethod.ERROR; }

    public void process(Message request, WriteProxy writeProxy) {
        Message response = writeProxy.getMessage();
        response.socketId = request.socketId;

        HttpHeaders headers = (HttpHeaders) request.metaData;

        if(!isAllowed(headers.httpMethod)) {
            response.writeToMessage(StorageService.cache.get("405"));
            return;
        }

        System.out.println("Url: " + headers.url);

        String filename = headers.url.substring(headers.url.lastIndexOf('/')+1);
        if(headers.url.startsWith("/chat")) {
            switchingToWs(headers, request, response, writeProxy);
            return;
        }
        else if(StorageService.cache.contains(filename)) {
            response.writeToMessage(StorageService.cache.get(filename).duplicate());
            writeProxy.enqueue(response);
            return;
        }

        var httpRequest = new HttpRequest();
        httpRequest.headers = headers;

        if(headers.bodyStartIndex < headers.bodyEndIndex) {
            HttpUtil.resolvePayload(request, httpRequest);
        }

        var res = router.forward(httpRequest);
        if(res.status < 0) {
            notifyChanges(res.json, response, writeProxy);
            return;
        }


        response.writeToMessage( respond(res) );
        writeProxy.enqueue(response);
    }

    private ByteBuffer respond(Response response) {
        if(response.status == 200) {
            if(response.json.hasKey("filename")) {
                if(StorageService.cache.contains(response.json.get("filename")))
                    return StorageService.cache.get(response.json.get("filename")).duplicate();
                else
                    return StorageService.cache.get("500");
            }
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

        // errors
        if(response.status == 400)
            return Responses.createBadRequest( response.json.hasKey("msg") ? response.json.get("msg") : "Bad request" );

        return StorageService.cache.get(String.valueOf(response.status)).duplicate();
    }

    private void switchingToWs(HttpHeaders headers, Message request, Message response, WriteProxy writeProxy) {
        headers.token = headers.url.split("token=",2)[1];
        headers.url = "/chat";
        headers.ws.socketId = request.socketId;

        var res = ChatController.onHttpHandle(headers);
        if(res.status == 101) {
            response.writeToMessage( Responses.createSwitchingProtocols(res.json.get("key")) );
            writeProxy.enqueue(response);

            User user = UserService.getById(Integer.parseInt(res.json.get("userId")));

            while(!user.pendingMessages.isEmpty()) {
                var msg = user.pendingMessages.remove();
                var newMsg = writeProxy.getMessage();
                newMsg.socketId = request.socketId;
                newMsg.writeToMessage(Responses.wsResponse((byte)-127, msg.asJsonString()));
                writeProxy.enqueue(newMsg);
            }
            return;
        }
        response.writeToMessage( StorageService.cache.get(String.valueOf(res.status)) );
        writeProxy.enqueue(response);
    }

    private void notifyChanges(Json res, Message response, WriteProxy writeProxy) {
        StringBuilder sb = new StringBuilder("[");
        for(var entry : res.asMap().entrySet()) {
            int targetSocketId = Integer.parseInt(entry.getKey());
            String formattedMatch = entry.getValue();
            if(targetSocketId > 0) { // is socketId
                var newNotification = writeProxy.getMessage();
                newNotification.socketId = targetSocketId;
                newNotification.writeToMessage(Responses.wsResponse((byte)-127,
                                            "{\"token\":\"" + formattedMatch.replace("\"", "'") + "\"}"));
                writeProxy.enqueue(newNotification);
            }
            else
                sb.append(formattedMatch).append(",");
        }
        if(sb.charAt(sb.length()-1) == ',')
            sb.setCharAt(sb.length()-1, ']');
        else
            sb.append("]");
        //sb.setCharAt(sb.length()-1, '\"');
        //sb.append("]\'");

        response.writeToMessage( Responses.createResponseBuffer( FileInfo.json(sb.toString().getBytes() ) ) );
        writeProxy.enqueue(response);
    }
}
