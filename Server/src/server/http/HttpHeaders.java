package server.http;


public class HttpHeaders {

    public EHttpMethod httpMethod = EHttpMethod.ERROR;
    public String url = "";

    public int hostStartIndex = 0;
    public int hostEndIndex   = 0;

    public String token       = null;
    public int contentLength  = 0;
    public String contentType = null;

    public int bodyStartIndex = 0;
    public int bodyEndIndex   = 0;


    @Override
    public String toString() {
        return  "httpMethod="       + httpMethod +
              ", url="              + url +
              ", token="            + token +
              ", hostStartIndex="   + hostStartIndex +
              ", hostEndIndex="     + hostEndIndex +
              ", contentLength="    + contentLength +
              ", bodyStartIndex="   + bodyStartIndex +
              ", bodyEndIndex="     + bodyEndIndex +
              '}';
    }
}
