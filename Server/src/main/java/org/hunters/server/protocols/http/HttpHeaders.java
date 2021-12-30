package org.hunters.server.protocols.http;


import org.hunters.server.MessageMetaData;

public class HttpHeaders extends MessageMetaData {

    public EHttpMethod httpMethod = EHttpMethod.ERROR;
    public String url = "";

    public int hostStartIndex = 0;
    public int hostEndIndex   = 0;

    public String token       = null;
    public int contentLength  = 0;
    public String contentType = null;
    public HttpWsHeaders ws   = null;
    //public String upgrade     = null;

    public int bodyStartIndex = 0;
    public int bodyEndIndex   = 0;


    @Override
    public String toString() {
        return                 httpMethod + " " + url + "\r\n" +
                               (ws.key != null ?  ws.key : "") +
            // (upgrade != null ? ", upgrade="          + upgrade : "") +
                               ", token="            + token +
                               ", hostStartIndex="   + hostStartIndex +
                               ", hostEndIndex="     + hostEndIndex +
                               ", contentLength="    + contentLength +
                               ", bodyStartIndex="   + bodyStartIndex +
                               ", bodyEndIndex="     + bodyEndIndex;
    }

    public static class HttpWsHeaders {
        public String key = null;
        public String protocol = null;

        public boolean isValid() {
            return key != null;
        }

        @Override
        public String toString() {
            return "Sec-WebSocket-Key: "      + key + "\r\n" +
                   "Sec-WebSocket-Protocol: " + protocol + "\r\n";
        }
    }
}
