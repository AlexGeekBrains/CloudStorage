package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CreateDirResponse implements Serializable {
    private static final long serialVersionUID = 1704884871847006444L;

    public enum CommandType {
        CREATE_DIR_OK("CREATE_DIR_OK"),
        CREATE_DIR_NO("CREATE_DIR_NO");
        private String command;

        CommandType(String command) {
            this.command = command;
        }
    }

    private String serverPath;
    private CreateDirResponse.CommandType command;
    private List<FileInfo> fileInfoList;
    private String freeSpaseStorage;
}