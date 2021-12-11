package server.utils;

import server.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class Responses {
    private Map<String, ByteBuffer> responseBuffers;
    private Path publicHtmlDir;

    public Responses(String pathDir, Map<String, ByteBuffer> cache) {
        this.responseBuffers = cache;
        this.publicHtmlDir = Paths.get(pathDir);
        try {
            fillLocalCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Responses(String pathDir) {
        this(pathDir, new HashMap<>());
    }

    public void fillLocalCache() throws IOException {

        for (Path p : Files.newDirectoryStream(publicHtmlDir)) {
            if (Files.isRegularFile(p)) {
                FileInfo fi = FileInfo.get(p, StandardCharsets.UTF_8);
                ByteBuffer responseBuffer = this.createResponseBuffer(fi);
                this.responseBuffers.put(p.getFileName().toString(), responseBuffer);
            }
        }
        // Create a special buffers
        this.responseBuffers.put("", this.responseBuffers.get("index.html"));
        this.responseBuffers.put("/", this.responseBuffers.get("index.html"));
        this.responseBuffers.put("204", this.createNoContent());
        this.responseBuffers.put("401", this.createUnauthorizedBuffer());
        this.responseBuffers.put("404", this.createNotFoundBuffer());
        this.responseBuffers.put("501", this.createNotImplementedBuffer());
    }


    public ByteBuffer createResponseBuffer(FileInfo fi) {
        ByteBuffer data = fi.getData();
        String header = "HTTP/1.1 200 OK\r\n"
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

    public ByteBuffer createHeaderOnlyBuf(String file) {
        byte[] header = null;
        try {
            FileInfo fi = FileInfo.get(Paths.get(Server.PUBLIC_HTML_DIR, file), StandardCharsets.UTF_8);
            header = ("HTTP/1.1 200 OK\r\n"
                    + "Server: DestSoulsServer v1.0\r\n"
                    + "Content-length: " + fi.getData().limit() + "\r\n"
                    + "Content-type: " + fi.getMIMEType() + "\r\n\r\n")
                    .getBytes(fi.getEncoding());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer buf = ByteBuffer.allocate(header.length);
        buf.put(header);
        buf.flip();
        return buf;
    }

    public ByteBuffer createUnauthorizedBuffer() {
        String uHeader = "HTTP/1.1 401 Unauthorized\r\n"
                + "Server: DestSoulsServer v1.0\r\n\r\n";
        byte[] nfHeaderData = uHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bufHeader = ByteBuffer.allocate(nfHeaderData.length);
        bufHeader.put(nfHeaderData);
        bufHeader.flip();
        return bufHeader;
    }

    public ByteBuffer createNotFoundBuffer() {
        String nfHeader = "HTTP/1.1 404 Not found\r\n"
                + "Server: DestSoulsServer v1.0\r\n\r\n";
        byte[] nfHeaderData = nfHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer nfBuffer = ByteBuffer.allocate(nfHeaderData.length);
        nfBuffer.put(nfHeaderData);
        nfBuffer.flip();
        return nfBuffer;
    }

    public ByteBuffer createNotImplementedBuffer() {
        String nfHeader = "HTTP/1.1 501 Not implemented\r\n"
                + "Server: DestSoulsServer v1.0\r\n\r\n";
        byte[] nfHeaderData = nfHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer nfBuffer = ByteBuffer.allocate(nfHeaderData.length);
        nfBuffer.put(nfHeaderData);
        nfBuffer.flip();
        return nfBuffer;
    }

    private ByteBuffer createNoContent() {
        byte[] header =
                "HTTP/1.1 204 No Content\r\nAllow: OPTIONS, GET, HEAD, POST\r\nServer: DestSoulsServer v1.0\r\n\r\n"
                        .getBytes(StandardCharsets.UTF_8);
        ByteBuffer nfBuffer = ByteBuffer.allocate(header.length);
        nfBuffer.put(header);
        nfBuffer.flip();
        return nfBuffer;
    }

    public ByteBuffer get(String key) {
        return this.responseBuffers.get(key);
    }

    public boolean contains(String buffer) {
        return this.responseBuffers.containsKey(buffer);
    }
}
