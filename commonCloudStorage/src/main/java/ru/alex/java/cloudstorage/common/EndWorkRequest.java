package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EndWorkRequest implements Serializable {
    private static final long serialVersionUID = -1071972198410992638L;
    private String login;
    private String password;

    public EndWorkRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}