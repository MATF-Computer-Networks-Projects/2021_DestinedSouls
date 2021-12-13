package server.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileInfo {

    public final static String PUBLIC_HTML_DIR = System.getenv("PUBLIC_HTML") != null ?
                                                                                System.getenv("PUBLIC_HTML")
                                                                             :  "public_html";

    public static FileInfo get(Path path, Charset encoding) throws IOException {
        try (var fin = new FileInputStream(path.toString())){
            FileChannel fc = fin.getChannel();
            ByteBuffer data = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            return new FileInfo(Files.probeContentType(path), encoding, data);
        } catch (IOException ex){
            ex.printStackTrace();
            throw ex;
        }
    }



    public static String getFilename(String filepath) {
        if(filepath.isEmpty())
            return "index.html";
        return Paths.get(PUBLIC_HTML_DIR, filepath).getFileName().toString();
    }

    public static long getSize(String file) {
        try {
            return Files.size(Paths.get(PUBLIC_HTML_DIR, file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isValid(String path) {
        try {
            return Files.probeContentType(Paths.get(PUBLIC_HTML_DIR, path.isEmpty() ? "index.html" : path)) != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static FileInfo json(Charset encoding, byte[] data) {
        return new FileInfo("application/json", encoding, ByteBuffer.wrap(data));
    }

    public static FileInfo json(byte[] data) {
        return new FileInfo("application/json", StandardCharsets.UTF_8, ByteBuffer.wrap(data));
    }

    private final String MIMEType;
    private final Charset encoding;
    private final ByteBuffer data;


    private FileInfo(String MIMEType, Charset encoding, ByteBuffer data) {
        this.MIMEType = MIMEType;
        this.encoding = encoding;
        this.data = data;
    }


    public String getMIMEType() {
        return this.MIMEType;
    }

    public Charset getEncoding() {
        return this.encoding;
    }

    public ByteBuffer getData() {
        return this.data;
    }
}
