package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class DeleteResponse implements Serializable {
    private static final long serialVersionUID = 914880954436570051L;

    public enum CommandType {
        DELETE_OK("DELETE_OK"),
        DELETE_NO("DELETE_NO");
        private String command;

        CommandType(String command) {
            this.command = command;
        }
    }

    private DeleteResponse.CommandType command;
    private List<FileInfo> fileInfoList;
    private String serverPath;
}