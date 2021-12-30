package org.hunters.server.http;

import org.hunters.server.Message;
import org.hunters.server.WriteProxy;
import org.hunters.server.utils.FileInfo;
import org.hunters.server.utils.Json;
import org.hunters.server.utils.Response;
import org.hunters.server.utils.Responses;
import org.hunters.server.IMessageProcessor;
import org.hunters.server.routes.Router;
import org.hunters.server.services.StorageService;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class HttpMessageProcessor implements IMessageProcessor {

    private final Router router;

    public HttpMessageProcessor(Router router) {
        this.router = router;
    }

    private boolean isAllowed(EHttpMethod method) { return method != EHttpMethod.ERROR; }


    @Override
    public void process(Message request, WriteProxy writeProxy) {

        Message response = writeProxy.getMessage();
        response.socketId = request.socketId;
        HttpHeaders headers = (HttpHeaders) request.metaData;

        if(!isAllowed(headers.httpMethod)) {
            response.writeToMessage(StorageService.cache.get("405"));
            writeProxy.enqueue(response);
            return;
        }

        System.out.println("Url: " + headers.url);

        String filename = headers.url.substring(headers.url.lastIndexOf('/')+1);
        if(StorageService.cache.contains(filename))
        {
            response.writeToMessage(StorageService.cache.get(filename).duplicate());
            writeProxy.enqueue(response);
            return;
        }

        System.out.println("Url: " + filename);



        var httpRequest = new HttpRequest();
        httpRequest.headers = headers;

        if(headers.bodyStartIndex < headers.bodyEndIndex) {
            resolvePayload(request, httpRequest);
        }

        response.writeToMessage( respond(router.forward(httpRequest) ));

        writeProxy.enqueue(response);
    }

    private static void resolvePayload(Message request, HttpRequest httpRequest) {
        if(httpRequest.headers.contentType.startsWith("multipart")) {
            resolveMultipart(request, httpRequest);
            return;
        }
        httpRequest.payload = new Json( new String( Arrays.copyOfRange(request.sharedArray,
                                                    httpRequest.headers.bodyStartIndex,
                                                    httpRequest.headers.bodyEndIndex)
                                                  )
        );
    }

    private static void resolveMultipart(Message request, HttpRequest httpRequest) {
        var httpHeaders = httpRequest.headers;

        int boundaryLength = httpHeaders.contentType.length() - httpHeaders.contentType.indexOf("----");
        httpHeaders.bodyStartIndex += boundaryLength + 2; // +2 for CRLF
        httpHeaders.bodyEndIndex -= boundaryLength + 2; // +2 for CRLF
        httpHeaders.contentLength -= 2 * boundaryLength + 4;

        int filenameIndex = HttpUtil.findNext(request.sharedArray,
                httpHeaders.bodyStartIndex, httpHeaders.bodyEndIndex, "filename=") + 10;
        int endLine = HttpUtil.findNextLineBreak(request.sharedArray, filenameIndex, httpHeaders.bodyEndIndex)-2; // "CR

        var filename = HttpUtil.sliceAsString(request.sharedArray, filenameIndex, endLine);
        httpRequest.other.put("filename", filename);

        endLine += 3;

        int newStart = HttpUtil.findNext(request.sharedArray, endLine, httpHeaders.bodyEndIndex, "\r\n\r\n") + 4;

        httpHeaders.contentLength -= newStart - httpHeaders.bodyStartIndex + 6;

        httpHeaders.bodyStartIndex = newStart;
        httpRequest.payload = Arrays.copyOfRange(request.sharedArray,
                httpHeaders.bodyStartIndex, httpHeaders.bodyStartIndex + httpHeaders.contentLength);
    }

    private ByteBuffer respond(Response response) {
        if(response.status == 200) {
            if(response.json.hasKey("filename"))
                return StorageService.cache.get(response.json.get("filename")).duplicate();
            return Responses.createResponseBuffer( FileInfo.json(response.json.toString().getBytes()) );
        }

        // errors
        if(response.status == 400)
            return Responses.createBadRequest( response.json.hasKey("msg") ? response.json.get("msg") : "Bad request" );

        return StorageService.cache.get(String.valueOf(response.status)).duplicate();
    }
}
