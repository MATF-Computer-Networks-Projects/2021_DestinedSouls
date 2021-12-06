package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

final class FileInfo {

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

    public static FileInfo json(Charset encoding, byte[] data) {
        return new FileInfo("application/json", encoding, ByteBuffer.wrap(data));
    }

    private final String MIMEType;
    private final Charset encoding;
    private final ByteBuffer data;


    private FileInfo(String MIMEType, Charset encoding, ByteBuffer data) {
        this.MIMEType = MIMEType;
        this.encoding = encoding;
        this.data = data;
    }


    String getMIMEType() {
        return this.MIMEType;
    }

    Charset getEncoding() {
        return this.encoding;
    }

    ByteBuffer getData() {
        return this.data;
    }
}
