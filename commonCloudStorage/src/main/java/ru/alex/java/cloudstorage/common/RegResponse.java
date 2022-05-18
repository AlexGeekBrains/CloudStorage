package ru.alex.java.cloudstorage.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
public class RegResponse implements Serializable {
    private static final long serialVersionUID = 538723090196690000L;
    private RegResponse.CommandType command;

    public enum CommandType {
        REG_OK("REG_OK"),
        REG_NO("REG_NO");
        private String command;

        CommandType(String command) {
            this.command = command;
        }
    }

    public RegResponse(RegResponse.CommandType command) {
        this.command = command;
    }
}