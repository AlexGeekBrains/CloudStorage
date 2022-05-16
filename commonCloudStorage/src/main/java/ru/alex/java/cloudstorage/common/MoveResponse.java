package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class MoveResponse implements Serializable {
    private static final long serialVersionUID = 2458225077636609676L;

    public enum CommandType {
        MOVE_FILE_FROM_CLIENT("MOVE_FILE_FROM_CLIENT"),
        MOVE_BIG_FILE_FROM_CLIENT("MOVE_BIG_FILE_FROM_CLIENT"),
        MOVE_FILE_FROM_SERVER("MOVE_FILE_FROM_SERVER"),
        MOVE_BIG_FILE_FROM_SERVER("MOVE_BIG_FILE_FROM_SERVER"),
        NO_MOVE_FILE_FROM_CLIENT("NO_MOVE_FILE_FROM_CLIENT");
        private String command;

        CommandType(String command) {
            this.command = command;
        }
    }

    private String clientPath;
    private String serverPath;
    private String fileName;
    private CommandType command;
    private List<FileInfo> fileInfoList;
    private boolean isFirstPartFile;
    private boolean isLastPartFile;
    private byte[] data;

    public MoveResponse(CommandType command) {
        this.command = command;
    }
}