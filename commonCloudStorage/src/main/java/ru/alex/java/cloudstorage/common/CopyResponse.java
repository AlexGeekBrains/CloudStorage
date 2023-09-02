package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class CopyResponse implements Serializable {
    private static final long serialVersionUID = 2730028430048677077L;

    public enum CommandType {
        COPY_FILE_FROM_CLIENT("COPY_FILE_FROM_CLIENT"),
        COPY_FILE_FROM_SERVER("COPY_FILE_FROM_SERVER"),
        COPY_BIG_FILE_FROM_CLIENT("COPY_BIG_FILE_FROM_CLIENT"),
        COPY_BIG_FILE_FROM_SERVER("COPY_BIG_FILE_FROM_SERVER"),
        COPY_DIR_FROM_CLIENT("COPY_DIR_FROM_CLIENT"),
        NO_COPY_FROM_CLIENT("COPY_NO_FROM_CLIENT");
        private String command;

        CommandType(String command) {
            this.command = command;
        }
    }

    private String clientPath;
    private String serverPath;
    private String fileName;
    private CopyResponse.CommandType command;
    private List<FileInfo> fileInfoList;
    private String freeSpaseStorage;
    private boolean isFirstPartFile;
    private boolean isLastPartFile;
    private byte[] data;

    public CopyResponse(CommandType command) {
        this.command = command;
    }
}