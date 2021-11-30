package server;

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
    public static void start(int port, String publicHtmlDir, int cacheAliveSeconds) {
        try {
            Server server = new Server(port, publicHtmlDir, cacheAliveSeconds);
            server.runServer();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private final int port;
    private final Path publicHtmlDir;
    public static Map<String, ByteBuffer> responseBuffers;
    private final int maxCacheAliveTime;


    private Server(int port, String publicHtmlDir, int cacheAliveSeconds) throws IOException {
        this.port = port;
        this.publicHtmlDir = Paths.get(publicHtmlDir);
        this.maxCacheAliveTime = cacheAliveSeconds * 1000;
        this.fillLocalCache();
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
                    this.fillLocalCache();
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

        System.out.println("Reading from client...");
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

    private void fillLocalCache() throws IOException {
        this.responseBuffers = new HashMap<>();


        for (Path p : Files.newDirectoryStream(this.publicHtmlDir)) {
            if (Files.isRegularFile(p)) {
                FileInfo fi = FileInfo.get(p, StandardCharsets.UTF_8);
                ByteBuffer responseBuffer = this.createResponseBuffer(fi);
                this.responseBuffers.put(p.getFileName().toString(), responseBuffer);
            }
        }

        // Create a special buffer to use when requested file is not found
        ByteBuffer nfBuffer = this.createNotFoundBuffer();
        this.responseBuffers.put("404", nfBuffer);
    }

    private ByteBuffer createResponseBuffer(FileInfo fi) {
        ByteBuffer data = fi.getData();
        String header = "HTTP/1.0 200 OK\r\n"
                + "Server: DestSoulsServer v1.0\r\n"
                + "Content-length: " + data.limit() + "\r\n"
                + "Content-type: " + fi.getMIMEType() + "\r\n\r\n";
        byte[] headerData = header.getBytes(fi.getEncoding());
        ByteBuffer buf = ByteBuffer.allocate(headerData.length + data.limit());
        buf.put(headerData);
        buf.put(data);
        buf.flip();
        return buf;
    }

    private ByteBuffer createNotFoundBuffer() {
        String nfHeader = "HTTP/1.0 404 Not found\r\n"
                + "Server: SimpleHTTP v1.0\r\n\r\n";
        byte[] nfHeaderData = nfHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer nfBuffer = ByteBuffer.allocate(nfHeaderData.length);
        nfBuffer.put(nfHeaderData);
        nfBuffer.flip();
        return nfBuffer;
    }
}
