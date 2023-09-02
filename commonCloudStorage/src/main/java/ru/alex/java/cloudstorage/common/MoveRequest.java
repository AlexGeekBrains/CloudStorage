package ru.alex.java.cloudstorage.common;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
@Setter
@Getter
public class MoveRequest implements Serializable {
    private static final long serialVersionUID = 1860256608158434053L;
    public enum CommandType {
        MOVE_FILE_FROM_CLIENT("MOVE_FILE_FROM_CLIENT"),
        MOVE_BIG_FILE_FROM_CLIENT("MOVE_BIG_FILE_FROM_CLIENT"),
        MOVE_FILE_FROM_SERVER("MOVE_FILE_FROM_SERVER");
        private String command;
        CommandType(String command) {
            this.command = command;
        }
    }
    private String login;
    private String password;
    private String clientPath;
    private String serverPath;
    private String fileName;
    private Long fileSize;
    private MoveRequest.CommandType command;
    private boolean isFirstPartFile;
    private boolean isLastPartFile;
    private byte[] data;
    public MoveRequest(CommandType command, String login, String password) {
        this.command = command;
        this.login = login;
        this.password = password;
    }
}