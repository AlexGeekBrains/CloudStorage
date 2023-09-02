package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CopyRequest implements Serializable {
    private static final long serialVersionUID = 3573436897752092266L;

    public enum CommandType {
        COPY_FILE_FROM_CLIENT("COPY_FILE_FROM_CLIENT"),
        COPY_FILE_FROM_SERVER("COPY_FILE_FROM_SERVER"),
        COPY_BIG_FILE_FROM_CLIENT("COPY_BIG_FILE_FROM_CLIENT"),
        COPY_DIR_FROM_CLIENT("COPY_DIR_FROM_CLIENT"),
        COPY_BIG_DIR_FROM_CLIENT("COPY_BIG_DIR_FROM_CLIENT");
        private String command;

        CommandType(String command) {
            this.command = command;
        }
    }

    private String login;
    private String password;
    private String clientPath;
    private String serverPath;
    private String fileName;
    private Long fileSize;
    private CopyRequest.CommandType command;
    private boolean isFirstPartFile;
    private boolean isLastPartFile;
    private byte[] data;

    public CopyRequest(CopyRequest.CommandType command, String login, String password) {
        this.command = command;
        this.login = login;
        this.password = password;
    }
}