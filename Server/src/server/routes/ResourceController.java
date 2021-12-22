package server.routes;

import server.Message;
import server.http.HttpHeaders;
import server.http.HttpUtil;
import server.security.Authorizer;
import server.services.StorageService;
import server.services.UserService;
import server.utils.FileInfo;
import server.utils.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


public class ResourceController implements IController {

    @Override
    public void get(Message request, Message response) {
        String url = ((HttpHeaders)request.metaData).url;
        String filename = FileInfo.getFilename(url);
        if (StorageService.cache.contains(filename)) {
            System.out.println("Resource controller: " + filename);
            response.writeToMessage(StorageService.cache.get(filename).duplicate());
            return;
        } else {
            var path = Paths.get(FileInfo.PUBLIC_HTML_DIR, url);
            if (Files.isRegularFile(path)) {
                try {
                    response.writeToMessage( StorageService.cache.createResponseBuffer(
                                                            FileInfo.get(path, StandardCharsets.UTF_8))
                                            );
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        response.writeToMessage( StorageService.cache.get("404").duplicate() );
    }

    @Override
    public void post(Message request, Message response) {
        var httpHeaders = (HttpHeaders)request.metaData;
        if(httpHeaders.token == null) {
            response.writeToMessage(StorageService.cache.get("401").duplicate());
        }

        int id = Integer.parseInt(Json.parseJSON(Authorizer.parseToken(httpHeaders.token)).get("sub"));

        if(httpHeaders.contentType.startsWith("multipart")) {
            int boundaryLength = httpHeaders.contentType.length() - httpHeaders.contentType.indexOf("----");

            //int dataStart = httpHeaders.bodyStartIndex + boundaryLength;
            httpHeaders.bodyStartIndex += boundaryLength + 2; // +2 for CRLF
            httpHeaders.bodyEndIndex   -= boundaryLength + 2; // +2 for CRLF
            httpHeaders.contentLength  -= 2 * boundaryLength + 4;
        }

        int filenameIndex = HttpUtil.findNext(request.sharedArray,
                                     httpHeaders.bodyStartIndex, httpHeaders.bodyEndIndex, "filename=") + 10;
        int endLine = HttpUtil.findNextLineBreak(request.sharedArray, filenameIndex, httpHeaders.bodyEndIndex)-2; // "CR

        var filename = HttpUtil.sliceAsString(request.sharedArray, filenameIndex, endLine);

        endLine += 3;
        int newStart = HttpUtil.findNext(request.sharedArray, endLine, httpHeaders.bodyEndIndex, "\r\n\r\n") + 4;

        httpHeaders.contentLength -= newStart - httpHeaders.bodyStartIndex + 6;

        httpHeaders.bodyStartIndex = newStart;
        byte[] rawFile = Arrays.copyOfRange(request.sharedArray,
                                httpHeaders.bodyStartIndex, httpHeaders.bodyStartIndex + httpHeaders.contentLength);

        System.out.println("[" + filename +"]\nContent-Lenght: " + rawFile.length);

        try {
            Path imgPath = StorageService.store(rawFile, filename);
            var res = UserService.addImage(id, imgPath);
            if(res.status == 200)
                response.writeToMessage(StorageService.cache.createResponseBuffer(
                                                                            FileInfo.json( res.json.getBytes() )));
            else
                response.writeToMessage(StorageService.cache.get("404").duplicate() );

        } catch (IOException e) {
            e.printStackTrace();
            response.writeToMessage(StorageService.cache.get("500").duplicate());
        }

    }

    @Override
    public void delete(Message request, Message response) {  }
}
