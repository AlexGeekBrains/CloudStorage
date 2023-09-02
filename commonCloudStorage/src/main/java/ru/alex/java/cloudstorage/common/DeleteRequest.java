package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -5822758574620758213L;
    private String login;
    private String password;
    private String serverPath;
    private String fileName;

    public DeleteRequest(String login, String password, String serverPath, String fileName) {
        this.login = login;
        this.password = password;
        this.serverPath = serverPath;
        this.fileName = fileName;
    }
}