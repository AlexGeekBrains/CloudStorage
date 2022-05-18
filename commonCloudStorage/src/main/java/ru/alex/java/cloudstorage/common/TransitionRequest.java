package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class TransitionRequest implements Serializable {
    private static final long serialVersionUID = -3105904549237414160L;

    public enum CommandType {
        NEXT_PATH("NEXT_PATH"),
        UP_PATH("UP_PATH");
        private String command;

        CommandType(String command) {
            this.command = command;
        }
    }

    private String login;
    private String password;
    private String serverPath;
    private TransitionRequest.CommandType command;

    public TransitionRequest(TransitionRequest.CommandType command, String login, String password, String serverPath) {
        this.command = command;
        this.login = login;
        this.password = password;
        this.serverPath = serverPath;
    }
}