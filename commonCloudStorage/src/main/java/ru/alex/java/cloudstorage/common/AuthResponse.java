package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AuthResponse implements Serializable {
    private static final long serialVersionUID = 9142214980214431594L;

    public enum CommandType {
        AUTH_OK("AUTH_OK"),
        AUTH_NO("AUTH_NO");
        private String command;

        CommandType(String command) {
            this.command = command;
        }
    }

    private String login;
    private String serverPath;
    private AuthResponse.CommandType command;
    private List<FileInfo> fileInfoList;
}