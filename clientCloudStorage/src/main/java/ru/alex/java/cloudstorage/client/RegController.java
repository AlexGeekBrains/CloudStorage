package ru.alex.java.cloudstorage.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.alex.java.cloudstorage.common.RegRequest;

import java.io.IOException;

public class RegController {
    private CloudStorageController cloudStorageController;

    public void setController(CloudStorageController cloudStorageController) {
        this.cloudStorageController = cloudStorageController;
    }

    @FXML
    public TextField loginReg;
    @FXML
    public PasswordField passReg;

    @FXML
    public void regBtn(ActionEvent actionEvent) throws IOException {
        if (loginReg.getText() != null && passReg.getText() != null) {
            RegRequest regRequest = new RegRequest(loginReg.getText().trim(), passReg.getText().trim());
            cloudStorageController.getNetwork().msg(regRequest);
        }
    }
}