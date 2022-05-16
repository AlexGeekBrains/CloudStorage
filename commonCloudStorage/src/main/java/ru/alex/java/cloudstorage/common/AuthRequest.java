package ru.alex.java.cloudstorage.common;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class AuthRequest implements Serializable {
    private static final long serialVersionUID = 8523751600693279532L;
    private String login;
    private String password;

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}