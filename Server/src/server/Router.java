package server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class Router {
    public static void httpRequestHandle(SelectionKey key, String req)
    {
        if (req.endsWith("\r\n\r\n")) {
            // Showing some functional concepts with collect() method,
            // this can be done easily using substring() method
            String httpMethod = req
                    .codePoints()
                    .takeWhile(c -> c > 32 && c < 127)
                    .collect(StringBuilder::new,
                            StringBuilder::appendCodePoint,
                            StringBuilder::append)
                    .toString()
                    ;
            String filename = req.substring(httpMethod.length()+1)
                    .codePoints()
                    .takeWhile(c -> c > 32 && c < 127)
                    .collect(StringBuilder::new,
                            StringBuilder::appendCodePoint,
                            StringBuilder::append)
                    .toString()
                    ;

            if(filename.equals("/"))
                filename = "index.html";
            if(filename.startsWith("/"))
                filename = filename.substring(1);

            if(httpMethod.equals("GET"))
                get(key, filename);


        }
    }

    public static void get(SelectionKey key, String filename)
    {
        System.out.println("Server received request for file: \"" + filename + '\"');
        // Get the response buffer
        if (Server.responseBuffers.containsKey(filename))
            key.attach(Server.responseBuffers.get(filename).duplicate());
        else
            key.attach(Server.responseBuffers.get("404").duplicate());

        // Change mode to write - now we will send response to this client
        key.interestOps(SelectionKey.OP_WRITE);
    }
}
