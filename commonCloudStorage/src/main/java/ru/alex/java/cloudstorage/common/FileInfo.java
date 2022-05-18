package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
public class FileInfo implements Serializable {
    private static final long serialVersionUID = -5329992732825315508L;

    @Getter
    public enum FileType {
        FILE("F"), DIRECTORY("D");
        private String name;

        FileType(String name) {
            this.name = name;
        }
    }

    private String filename;
    private FileType type;
    private long size;
    private LocalDateTime lastModified;

    public FileInfo(Path path) {
        try {
            this.filename = path.getFileName().toString();
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }
}