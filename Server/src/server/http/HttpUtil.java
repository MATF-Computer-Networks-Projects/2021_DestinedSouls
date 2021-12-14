package server.http;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

        while(endOfHeader != -1 && endOfHeader != prevEndOfHeader + 1){

            if(matches(src, prevEndOfHeader, "content-length")){
                try {
                    findContentLength(src, prevEndOfHeader, endIndex, httpHeaders);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if(matches(src, prevEndOfHeader, "authorization")) {
                resolveAuthToken(src, prevEndOfHeader, endIndex, httpHeaders);
            }

            prevEndOfHeader = endOfHeader + 1;
            endOfHeader = findNextLineBreak(src, prevEndOfHeader, endIndex);
        }

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
        int indexOfColon = findNext(src, startIndex, endIndex, (byte) ':');

        //skip spaces after colon
        int index = indexOfColon +1;
        while(src[index] == ' '){
            index++;
        }

        int valueStartIndex = index;
        int valueEndIndex   = index;
        boolean endOfValueFound = false;

        while(index < endIndex && !endOfValueFound){
            switch(src[index]){
                case '0' : ;
                case '1' : ;
                case '2' : ;
                case '3' : ;
                case '4' : ;
                case '5' : ;
                case '6' : ;
                case '7' : ;
                case '8' : ;
                case '9' : { index++;  break; }

                default: {
                    endOfValueFound = true;
                    valueEndIndex = index;
                }
            }
        }

        httpHeaders.contentLength = Integer.parseInt(new String(src, valueStartIndex, valueEndIndex - valueStartIndex, "UTF-8"));

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

        var token = new ArrayList<Byte>();
        int endIdx = findNextLineBreak(src, index, endIndex) - 1;

        httpHeaders.token = new String(Arrays.copyOfRange(src,index, endIdx), StandardCharsets.UTF_8);
    }
}
