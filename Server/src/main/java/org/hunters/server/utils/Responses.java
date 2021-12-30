package org.hunters.server.utils;

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
                ByteBuffer responseBuffer = createResponseBuffer(fi);
                this.responseBuffers.put(p.getFileName().toString(), responseBuffer);
            }
        }
        // Create a special buffers
        this.responseBuffers.put("", this.responseBuffers.get("index.html"));
        this.responseBuffers.put("/", this.responseBuffers.get("index.html"));
        this.responseBuffers.put("204", this.createNoContent());
        this.responseBuffers.put("401", this.createUnauthorizedBuffer());
        this.responseBuffers.put("403", this.createForbidden());
        this.responseBuffers.put("404", this.createNotFoundBuffer());
        this.responseBuffers.put("405", this.createNotAllowed());
        this.responseBuffers.put("500", this.createInternalServerError());
        this.responseBuffers.put("501", this.createNotImplementedBuffer());
    }


    public static ByteBuffer createResponseBuffer(FileInfo fi) {
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
            FileInfo fi = FileInfo.get(Paths.get(FileInfo.PUBLIC_HTML_DIR, file), StandardCharsets.UTF_8);
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
    private ByteBuffer createNoContent() {
        byte[] header =
                "HTTP/1.1 204 No Content\r\nAllow: OPTIONS, GET, HEAD, POST\r\nServer: DestSoulsServer v1.0\r\n\r\n"
                        .getBytes(StandardCharsets.UTF_8);
        ByteBuffer nfBuffer = ByteBuffer.allocate(header.length);
        nfBuffer.put(header);
        nfBuffer.flip();
        return nfBuffer;
    }

    public static ByteBuffer createBadRequest(String msg) {
        byte[] nfHeaderData = ("HTTP/1.1 400 Bad Request\r\nServer: DestSoulsServer v1.0\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8);
        if(msg.startsWith("\"msg\":"))
                msg = "{" + msg + "}";
        else if(!msg.startsWith("{"))
            msg = "{\"msg\":\"" + msg + "\"}";
        byte[] bMsg = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(nfHeaderData.length + bMsg.length);
        buf.put(nfHeaderData);
        buf.put(bMsg);
        buf.flip();
        return buf;
    }

    private ByteBuffer createUnauthorizedBuffer() {
        String uHeader = "HTTP/1.1 401 Unauthorized\r\n"
                + "Server: DestSoulsServer v1.0\r\n\r\n";
        byte[] nfHeaderData = uHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bufHeader = ByteBuffer.allocate(nfHeaderData.length);
        bufHeader.put(nfHeaderData);
        bufHeader.flip();
        return bufHeader;
    }

    private ByteBuffer createForbidden() {
        byte[] nfHeaderData = ("HTTP/1.1 403 Forbidden\r\nServer: DestSoulsServer v1.0\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(nfHeaderData.length);
        buf.put(nfHeaderData);
        buf.flip();
        return buf;
    }

    private ByteBuffer createNotFoundBuffer() {
        String nfHeader = "HTTP/1.1 404 Not found\r\n"
                + "Server: DestSoulsServer v1.0\r\n\r\n";
        byte[] nfHeaderData = nfHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer nfBuffer = ByteBuffer.allocate(nfHeaderData.length);
        nfBuffer.put(nfHeaderData);
        nfBuffer.flip();
        return nfBuffer;
    }


    private ByteBuffer createNotAllowed() {
        String nfHeader = "HTTP/1.1 405 Method Not Allowed\r\n"
                + "Server: DestSoulsServer v1.0\r\n\r\n";
        byte[] nfHeaderData = nfHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer nfBuffer = ByteBuffer.allocate(nfHeaderData.length);
        nfBuffer.put(nfHeaderData);
        nfBuffer.flip();
        return nfBuffer;
    }

    private ByteBuffer createInternalServerError() {
        String nfHeader = "HTTP/1.1 500 Internal Server Error\r\n"
                + "Server: DestSoulsServer v1.0\r\n\r\n";
        byte[] nfHeaderData = nfHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer nfBuffer = ByteBuffer.allocate(nfHeaderData.length);
        nfBuffer.put(nfHeaderData);
        nfBuffer.flip();
        return nfBuffer;
    }

    private ByteBuffer createNotImplementedBuffer() {
        String nfHeader = "HTTP/1.1 501 Not implemented\r\n"
                + "Server: DestSoulsServer v1.0\r\n\r\n";
        byte[] nfHeaderData = nfHeader.getBytes(StandardCharsets.UTF_8);
        ByteBuffer nfBuffer = ByteBuffer.allocate(nfHeaderData.length);
        nfBuffer.put(nfHeaderData);
        nfBuffer.flip();
        return nfBuffer;
    }

    public ByteBuffer get(String key) {
        return this.responseBuffers.get(key);
    }

    // TODO: Add int keys

    public boolean contains(String buffer) {
        return this.responseBuffers.containsKey(buffer);
    }

    public void put(String key, ByteBuffer buffer) {
        this.responseBuffers.put(key, buffer);
    }
}
