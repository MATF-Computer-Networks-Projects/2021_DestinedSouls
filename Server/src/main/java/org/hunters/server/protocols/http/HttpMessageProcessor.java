package org.hunters.server.protocols.http;

import org.hunters.server.Message;
import org.hunters.server.MessageProcessor;
import org.hunters.server.WriteProxy;
import org.hunters.server.models.users.User;
import org.hunters.server.routes.ChatController;
import org.hunters.server.routes.Router;
import org.hunters.server.services.StorageService;
import org.hunters.server.services.UserService;
import org.hunters.server.utils.FileInfo;
import org.hunters.server.utils.Response;
import org.hunters.server.utils.Responses;

import java.nio.ByteBuffer;

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


        response.writeToMessage( respond(res) );
        writeProxy.enqueue(response);
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
            User user = UserService.getById(Integer.parseInt(res.json.get("userId")));
            response.writeToMessage( Responses.createSwitchingProtocols(res.json.get("key")) );
            writeProxy.enqueue(response);
            /*
            while(!user.pendingMessages.isEmpty()) {
                var msg = user.pendingMessages.remove();
                var newMsg = writeProxy.getMessage();
                newMsg.socketId = request.socketId;
                newMsg.writeToMessage(Responses.wsResponse((byte)-127, msg.asJsonString()));
                writeProxy.enqueue(newMsg);
            }
             */
        }
    }
}
