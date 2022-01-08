package org.hunters.server.protocols.ws;

import org.hunters.server.Message;
import org.hunters.server.MessageProcessor;
import org.hunters.server.WriteProxy;
import org.hunters.server.routes.ChatController;
import org.hunters.server.services.StorageService;
import org.hunters.server.utils.Responses;

import java.util.Arrays;

public class WsMessageProcessor {

    public void process(Message request, WriteProxy writeProxy) {
        Message response = writeProxy.getMessage();
        response.socketId = request.socketId;

        var frame = ((WsHeaders)request.metaData).frame;


        String req = appendSocketId(request, (int) (request.offset+frame.length));
        if(req == null) {
            response.writeToMessage(StorageService.cache.get("1003").duplicate());
            writeProxy.enqueue(response);
            return;
        }

        var res = (new ChatController()).handle(req);

        if(res.status != 0) {
            response.writeToMessage(StorageService.cache.get("1003").duplicate());
            writeProxy.enqueue(response);
            return;
        }

        //response.writeToMessage(Responses.wsResponse(frame.encoded(), "{}"));
        //writeProxy.enqueue(response);
        var responseToSocket = writeProxy.getMessage();
        responseToSocket.socketId = request.socketId;
        responseToSocket.writeToMessage(Responses.wsResponse(frame.encoded(), "{\"id\":\"" + res.json.get("id") + "\"}"));
        writeProxy.enqueue(responseToSocket);

        int targetSocketId = Integer.parseInt(res.json.remove("socketId"));
        response.socketId = targetSocketId;
        if(targetSocketId != -1) {
            response.writeToMessage( Responses.wsResponse(frame.encoded(),  res.json.toString() ) );
            writeProxy.enqueue(response);
        }
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

}
