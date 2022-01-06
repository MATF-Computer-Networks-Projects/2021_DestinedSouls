package org.hunters.server.protocols.http;


import org.hunters.server.Message;
import org.hunters.server.utils.Json;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class HttpUtil {

    private static final byte[] GET    = new byte[]{'G','E','T'};
    private static final byte[] POST   = new byte[]{'P','O','S','T'};
    private static final byte[] PUT    = new byte[]{'P','U','T'};
    private static final byte[] HEAD   = new byte[]{'H','E','A','D'};
    private static final byte[] DELETE = new byte[]{'D','E','L','E','T','E'};

    public static int parseHttpRequest(byte[] src, int startIndex, int endIndex, HttpHeaders httpHeaders){



        int endOfHttpMethod = findNext(src, startIndex, endIndex, (byte) ' ');
        if(endOfHttpMethod == -1)
            return -1;

        startIndex += resolveHttpMethod(src, startIndex, httpHeaders);
        if(httpHeaders.httpMethod == EHttpMethod.ERROR)
            return -1;
        resolveUrl(src, startIndex+1, httpHeaders);

        //parse HTTP request line
        int endOfFirstLine = findNextLineBreak(src, startIndex, endIndex);
        if(endOfFirstLine == -1)
            return -1;


        //parse HTTP headers
        int prevEndOfHeader = endOfFirstLine + 1;
        int endOfHeader = findNextLineBreak(src, prevEndOfHeader, endIndex);
        HttpHeaders.HttpWsHeaders wsHeaders = new HttpHeaders.HttpWsHeaders();
        while(endOfHeader != -1 && endOfHeader != prevEndOfHeader + 1) {

            if(matches(src, prevEndOfHeader, "content-length")){
                try {
                    findContentLength(src, prevEndOfHeader, endIndex, httpHeaders);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            else if(matches(src, prevEndOfHeader, "authorization"))
                resolveAuthToken(src, prevEndOfHeader, endIndex, httpHeaders);
            else if(matches(src, prevEndOfHeader, "content-type"))
                resolveContentType(src, prevEndOfHeader, endIndex, httpHeaders);
            else if(matches(src, prevEndOfHeader, "sec-")) {
                if(matches(src, prevEndOfHeader, "sec-websocket-key"))
                    wsHeaders.key = new String(getHeaderValue(src, prevEndOfHeader, endIndex), StandardCharsets.UTF_8);
                else if(matches(src, prevEndOfHeader, "sec-websocket-protocol"))
                    wsHeaders.protocol = new String(getHeaderValue(src, prevEndOfHeader, endIndex), StandardCharsets.UTF_8);
            }

            prevEndOfHeader = endOfHeader + 1;
            endOfHeader = findNextLineBreak(src, prevEndOfHeader, endIndex);
        }
        if(wsHeaders.isValid())
            httpHeaders.ws = wsHeaders;

        if(endOfHeader == -1){
            return -1;
        }

        //check that byte array contains full HTTP message.
        int bodyStartIndex = endOfHeader + 1;
        int bodyEndIndex  = bodyStartIndex + httpHeaders.contentLength;

        if(bodyEndIndex <= endIndex){
            //byte array contains a full HTTP request

            httpHeaders.bodyStartIndex = bodyStartIndex;
            httpHeaders.bodyEndIndex   = bodyEndIndex;
            return bodyEndIndex;
        }


       return -1;
    }

    private static void findContentLength(byte[] src, int startIndex, int endIndex, HttpHeaders httpHeaders) throws UnsupportedEncodingException {
        int index = getValueStartIndex(src, startIndex, endIndex);

        int valueStartIndex = index;
        boolean endOfValueFound = false;

        while(index < endIndex && !endOfValueFound){
            if(src[index] >= '0' && src[index] <= '9')
                ++index;
            else
                endOfValueFound = true;
        }

        httpHeaders.contentLength = Integer.parseInt(new String(src, valueStartIndex, index - valueStartIndex));
    }


    public static int findNext(byte[] src, int startIndex, int endIndex, byte value){
        for(int index = startIndex; index < endIndex; index++){
            if(src[index] == value) return index;
        }
        return -1;
    }

    public static int findNextLineBreak(byte[] src, int startIndex, int endIndex) {
        for(int index = startIndex; index < endIndex; index++){
            if(src[index] == '\n'){
                if(src[index - 1] == '\r'){
                    return index;
                }
            };
        }
        return -1;
    }

    public static int findNext(byte[] src, int startIndex, int endIndex, String value) {
        for(int index = startIndex; index < endIndex; index++){
            if(matches(src, index, value)) return index;
        }
        return -1;
    }

    public static int resolveHttpMethod(byte[] src, int startIndex, HttpHeaders httpHeaders){
        if(matches(src, startIndex, GET)) {
            httpHeaders.httpMethod = EHttpMethod.GET;
            return 3;
        }
        if(matches(src, startIndex, POST)){
            httpHeaders.httpMethod = EHttpMethod.POST;
            return 4;
        }
        if(matches(src, startIndex, PUT)){
            httpHeaders.httpMethod = EHttpMethod.PUT;
            return 3;
        }
        if(matches(src, startIndex, HEAD)){
            httpHeaders.httpMethod = EHttpMethod.HEAD;
            return 4;
        }
        if(matches(src, startIndex, DELETE)){
            httpHeaders.httpMethod = EHttpMethod.DELETE;
            return 6;
        }
        else
            httpHeaders.httpMethod = EHttpMethod.ERROR;
        return -1;
    }

    public static boolean matches(byte[] src, int offset, String value){
        for(int i=offset, n=0; n < value.length(); i++, n++){
            if(Character.toLowerCase((char)src[i]) != value.charAt(n)) return false;
        }
        return true;
    }

    public static boolean matches(byte[] src, int offset, byte[] value){
        for(int i=offset, n=0; n < value.length; i++, n++){
            if(src[i] != value[n]) return false;
        }
        return true;
    }

    public static void resolveUrl(byte[] src, int offset, HttpHeaders httpHeaders) {
        var sb = new StringBuilder();
        byte space = (char)' ';
        while (src[offset] != space)
            sb.append((char)src[offset++]);
        httpHeaders.url = sb.toString();
    }

    private static void resolveAuthToken(byte[] src, int startIndex, int endIndex, HttpHeaders httpHeaders) {
        int indexOfColon = findNext(src, startIndex, endIndex, (byte) ':');

        //skip spaces after colon
        int index = indexOfColon +1;
        while(src[index] == ' '){
            ++index;
        }

        if(!matches(src, index, "bearer"))
            return;

        index += 6;
        while(src[index] == ' ') {
            ++index;
        }

        int endIdx = findNextLineBreak(src, index, endIndex) - 1;

        httpHeaders.token = new String(Arrays.copyOfRange(src,index, endIdx), StandardCharsets.UTF_8);
    }

    private static void resolveContentType(byte[] src, int startIndex, int endIndex, HttpHeaders httpHeaders) {

        int indexOfColon = findNext(src, startIndex, endIndex, (byte) ':');

        //skip spaces after colon
        int index = indexOfColon +1;
        while(src[index] == ' '){
            ++index;
        }

        int endIdx = findNextLineBreak(src, index, endIndex) - 1;
        httpHeaders.contentType = new String(Arrays.copyOfRange(src,index, endIdx), StandardCharsets.UTF_8);
    }

    public static String sliceAsString(byte[] src, int startIndex, int endIndex) {
        if(startIndex < 0 || endIndex >= src.length)
            return null;

        return new String(Arrays.copyOfRange(src,startIndex, endIndex), StandardCharsets.UTF_8);
    }

    public static void resolvePayload(Message request, HttpRequest httpRequest) {
        if(httpRequest.headers.contentType.startsWith("multipart")) {
            resolveMultipart(request, httpRequest);
            return;
        }
        httpRequest.payload = new Json( new String( Arrays.copyOfRange(request.sharedArray,
                                                    httpRequest.headers.bodyStartIndex,
                                                    httpRequest.headers.bodyEndIndex)
                                                   ));
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
        httpRequest.setValue("filename", filename);

        endLine += 3;

        int newStart = HttpUtil.findNext(request.sharedArray, endLine, httpHeaders.bodyEndIndex, "\r\n\r\n") + 4;

        httpHeaders.contentLength -= newStart - httpHeaders.bodyStartIndex + 6;

        httpHeaders.bodyStartIndex = newStart;
        httpRequest.payload = Arrays.copyOfRange(request.sharedArray,
                httpHeaders.bodyStartIndex, httpHeaders.bodyStartIndex + httpHeaders.contentLength);
    }

    private static int getValueStartIndex(byte[] src, int startIndex, int endIndex) {
        int index = findNext(src, startIndex, endIndex, (byte) ':');
        if(index == -1)
            return -1;
        //skip spaces after colon
        ++index;
        while(src[index] == ' ')
            ++index;

        return index;
    }

    private static byte[] getHeaderValue(byte[] src, int startIndex, int endIndex) {
        startIndex = getValueStartIndex(src, startIndex, endIndex);
        endIndex = findNextLineBreak(src, startIndex, endIndex) - 1;
        return Arrays.copyOfRange(src,startIndex, endIndex);
    }
}
