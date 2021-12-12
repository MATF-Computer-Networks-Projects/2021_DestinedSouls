package server.middleware;

import server.utils.Parsers;

import java.nio.ByteBuffer;

public class StorageHandler {
    public static String getFilename(String data) {
        return Parsers.getFilename(data);
    }

    public static void store(String filename, String data) {
        ByteBuffer bf = ByteBuffer.allocate(data.length())
    }
}
