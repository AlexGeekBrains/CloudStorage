package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CreateDirRequest implements Serializable {
    private static final long serialVersionUID = -8562206370214059511L;
    private String login;
    private String password;
    private String dirName;
    private String serverPath;

    public CreateDirRequest(String login, String password, String dirName, String serverPath) {
        this.login = login;
        this.password = password;
        this.dirName = dirName;
        this.serverPath = serverPath;
    }
}