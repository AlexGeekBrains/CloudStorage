package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RegRequest implements Serializable {
    private static final long serialVersionUID = -4724189823091873960L;
    private String login;
    private String password;

    public RegRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}