package com.luv2code.jobportal.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDownloadUtil {

    private Path foundfile;

    public Resource getFileAsResource(String downloadDir, String fileName) throws IOException {

        // get a path to that directory that download directory
        Path path = Paths.get(downloadDir);
        // go through and find that file
        Files.list(path).forEach(file -> {
            if (file.getFileName().toString().startsWith(fileName)) {
                foundfile = file;
            }
        });

        // make sure that i found it: foundfile is not equal null. we simply give a reference to the location of that file
        if (foundfile != null) {
            return new UrlResource(foundfile.toUri());
        }

        return null;
    }
}
