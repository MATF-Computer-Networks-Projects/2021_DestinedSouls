package org.hunters.server.services;

import org.hunters.server.utils.FileInfo;
import org.hunters.server.utils.Responses;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageService {
    public static final Responses cache       = new Responses(FileInfo.PUBLIC_HTML_DIR);
    public static final Path      uploadsDir  = Paths.get( System.getenv("PUBLIC_UPLOADS") != null
                                                    ? System.getenv("PUBLIC_UPLOADS")
                                                    : "public_uploads" );

    public static void resetLocalCache() throws IOException {
        StorageService.cache.fillLocalCache();
    }

    public static Path store(byte[] rawFile, String path) throws IOException {
        if(!Files.exists(uploadsDir))
            Files.createDirectories(uploadsDir);

        return Files.write(Paths.get(uploadsDir.toString(),
                            String.valueOf(System.currentTimeMillis()) + '_' +path), rawFile);
    }
}
