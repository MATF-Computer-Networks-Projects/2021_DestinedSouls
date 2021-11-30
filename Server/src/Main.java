import server.Server;

import java.util.Arrays;

public class Main {
    private static final int PORT = 3000;
    private static String PUBLIC_HTML_DIR = "public_html";
    private static final int CACHE_TIMEOUT = 10000;

    public static void main(String[] args) {
        
        if(Arrays.asList(args).contains("--development"))
            PUBLIC_HTML_DIR = "Client/dist/client";

        Server.start(PORT, PUBLIC_HTML_DIR, CACHE_TIMEOUT);
    }
}
