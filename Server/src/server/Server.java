package server;

import server.services.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

final public class Server {
    public static String PUBLIC_HTML_DIR = "public_html";
    public static void start(int port, String publicHtmlDir, int cacheAliveSeconds) {
        try {
            Server server = new Server(port, publicHtmlDir, cacheAliveSeconds);
            UserService.load();
            server.runServer();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private final int port;
    // public static final String publicHtmlDir;
    private final int maxCacheAliveTime;


    private Server(int port, String publicHtmlDir, int cacheAliveSeconds) throws IOException {
        this.port = port;
        // Router.publicHtmlDir = publicHtmlDir;
        this.maxCacheAliveTime = cacheAliveSeconds * 1000;
    }

    private void runServer() throws IOException {
        try(ServerSocketChannel serverChannel = ServerSocketChannel.open();
            Selector selector = Selector.open();
        ) {
            serverChannel.bind(new InetSocketAddress(this.port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started.");

            long lastCacheUpdateTime = System.currentTimeMillis();
            int clients = 0;


            //noinspection InfiniteLoopStatement
            while (true) {
                if (clients == 0 && System.currentTimeMillis() - lastCacheUpdateTime >= this.maxCacheAliveTime) {
                    System.out.println("Updating server cache...");
                    //responseBuffers.fillLocalCache();
                    lastCacheUpdateTime = System.currentTimeMillis();
                }

                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while(it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    try {
                        if (key.isAcceptable()) {
                            this.acceptClient(key, selector);
                            clients++;
                        } else if (key.isReadable()) {
                            this.readRequestFromClient(key);
                        } else if(key.isWritable()) {
                            if (this.writeToClient(key))
                                clients--;
                        }
                    } catch(IOException ex) {
                        key.cancel();
                        clients--;
                        try {
                            key.channel().close();
                        } catch (IOException cex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void acceptClient(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel)key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted.");
    }

    private void readRequestFromClient(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel)key.channel();

        // If we are reading from this client for the first time,
        // we create a buffer for his request
        ByteBuffer buf = (ByteBuffer)key.attachment();
        if (buf == null) {

            buf = ByteBuffer.allocate(4096);
            key.attach(buf);
        }

        System.out.println("Reading from client: " + client.getRemoteAddress());
        client.read(buf);

        String maybeCompleteRequest = new String(buf.array(), 0, buf.position());

        Router.httpRequestHandle(key, maybeCompleteRequest);
    }

    private boolean writeToClient(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel)key.channel();
        ByteBuffer buffer = (ByteBuffer)key.attachment();

        System.out.println("Writing to client...");
        client.write(buffer);

        if (!buffer.hasRemaining()) {
            // Per HTTP, if we are done with response, we close connection
            System.out.println("Finished working with the client.");
            client.close();
            return true;
        }

        // This method returns finished indicator
        return false;
    }


}
