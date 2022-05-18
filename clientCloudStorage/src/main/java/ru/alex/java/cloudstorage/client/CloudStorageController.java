package ru.alex.java.cloudstorage.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import ru.alex.java.cloudstorage.common.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CloudStorageController implements Initializable {
    private ClientNetwork network;
    private final static String ROOT = "clientCloudStorage/directoryClient";
    private Stage stage;
    private Stage regStage;
    private RegController regController;
    private String login;
    private String password;
    private static final int MB_19 = 19 * 1_000_000;
    @FXML
    private TextField freeSpaseField;
    @FXML
    private TextField newDirField;
    @FXML
    private TableView<FileInfo> filesClientTable;
    @FXML
    private TableView<FileInfo> filesServerTable;
    @FXML
    private TextField pathFieldServer;
    @FXML
    private Pane regPanel;
    @FXML
    private TextField authLogin;
    @FXML
    private PasswordField authPass;
    @FXML
    private ComboBox<String> disksBoxClient;
    @FXML
    private TextField pathFieldClient;
    @FXML
    private VBox cloudStoragePanel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        network = new ClientNetwork(this);
        Platform.runLater(() -> {
            stage = (Stage) pathFieldClient.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                EndWorkRequest request = new EndWorkRequest(login, password);
                System.out.println("bye");
                network.msg(request);
            });
        });
        createClientSideColumn();
        createServerSideColumn();
        disksBoxClient.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBoxClient.getItems().add(p.toString());
        }
        disksBoxClient.getSelectionModel().select(0);
        updateClientList(Paths.get(ROOT));
        nextDirClientSide();
        nextDirServerSide();
    }

    private void createClientSideColumn() {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);
        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(240);
        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);
        filesClientTable.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn);
        filesClientTable.getSortOrder().add(fileTypeColumn);
    }

    private void createServerSideColumn() {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);
        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(240);
        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);
        filesServerTable.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn);
        filesServerTable.getSortOrder().add(fileTypeColumn);
    }

    public void btnExitAction(ActionEvent actionEvent) {
        EndWorkRequest request = new EndWorkRequest(login, password);
        network.msg(request);
        Platform.exit();
    }

    @FXML
    private void authBtn() throws IOException {
        if (authLogin.getText() != null && authPass.getText() != null) {
            login = authLogin.getText().trim();
            password = authPass.getText().trim();
            AuthRequest authRequest = new AuthRequest(login, password);
            network.msg(authRequest);
        }
    }

    public void authAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "неверный логин или пароль", ButtonType.OK);
        alert.showAndWait();
    }

    public void regAlertSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Регистрация пройдена успешно, авторизуйтесь для дальнейшей работы", ButtonType.OK);
        alert.showAndWait();
        regStage.close();
    }

    public void regAlertFailure() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Пользователь с таким именем уже существует", ButtonType.OK);
        alert.showAndWait();
    }

    public void delAlertFailure() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "не удалось удалить выбранный файл", ButtonType.OK);
        alert.showAndWait();
    }

    public void createNewDirFailure() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Вы пытаетесь создать слишком большую вложенность", ButtonType.OK);
        alert.showAndWait();
    }

    public void copyAlertFailure() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "В хранилище CloudStorage недостаточно свободного места. Увеличьте хранилище", ButtonType.OK);
        alert.showAndWait();
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Registration.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Cloud storage");
            regStage.setScene(new Scene(root, 500, 425));
            regStage.initModality(Modality.APPLICATION_MODAL);
            regController = fxmlLoader.getController();
            regController.setController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void regBtn(ActionEvent actionEvent) throws IOException {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    public void setTitle(String nickname) {
        String title;
        if (nickname.equals("")) {
            title = "Cloud storage";
        } else {
            title = String.format("Cloud storage - %s", nickname);
        }
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

    public void updateClientList(Path path) {
        try {
            pathFieldClient.setText(path.normalize().toAbsolutePath().toString());
            filesClientTable.getItems().clear();
            filesClientTable.getItems().addAll(Files.list(path)
                    .map(FileInfo::new)
                    .collect(Collectors.toList()));
            filesClientTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void updateServerList(String path, List<FileInfo> fileInfo) {
        pathFieldServer.setText(path);
        filesServerTable.getItems().clear();
        filesServerTable.getItems().addAll(fileInfo);
        filesServerTable.sort();
    }

    public void btnUpClientSide(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathFieldClient.getText()).getParent();
        if (upperPath != null) {
            updateClientList(upperPath);
        }
    }

    private void nextDirClientSide() {
        filesClientTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(pathFieldClient.getText())
                            .resolve(filesClientTable.getSelectionModel()
                                    .getSelectedItem().getFilename());
                    if (Files.isDirectory(path)) {
                        updateClientList(path);
                    }
                }
            }
        });
    }

    private void nextDirServerSide() {
        filesServerTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    if (getSelectedFileTypeServerSide().equals(FileInfo.FileType.DIRECTORY)) {
                        String nextPath = Paths.get(pathFieldServer.getText())
                                .resolve(filesServerTable.getSelectionModel()
                                        .getSelectedItem().getFilename()).toString();
                        TransitionRequest transitionRequest = new TransitionRequest(TransitionRequest.CommandType.NEXT_PATH, login, password, nextPath);
                        network.msg(transitionRequest);
                    }
                }
            }
        });
    }

    public void btnUpServerSide(ActionEvent actionEvent) {
        try {
            String upperPath = Paths.get(pathFieldServer.getText()).getParent().toString();
            TransitionRequest transitionRequest = new TransitionRequest(TransitionRequest.CommandType.UP_PATH, login, password, upperPath);
            network.msg(transitionRequest);
        } catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Вы находитесь в корневом каталоге", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void showWorkArea() {
        this.regPanel.setVisible(false);
        this.regPanel.setManaged(false);
        this.cloudStoragePanel.setVisible(true);
        this.cloudStoragePanel.setManaged(true);
    }

    public ClientNetwork getNetwork() {
        return network;
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateClientList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFilenameClientSide() {
        if (!filesClientTable.isFocused()) {
            return null;
        }
        return filesClientTable.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getSelectedFilenameServerSide() {
        if (!filesServerTable.isFocused()) {
            return null;
        }
        return filesServerTable.getSelectionModel().getSelectedItem().getFilename();
    }

    public Enum<FileInfo.FileType> getSelectedFileTypeServerSide() {
        if (!filesServerTable.isFocused()) {
            return null;
        }
        return filesServerTable.getSelectionModel().getSelectedItem().getType();
    }

    public String getCurrentPathClientSide() {
        return pathFieldClient.getText();
    }

    public String getCurrentPathServerSide() {
        return pathFieldServer.getText();
    }

    public void btnDelete(ActionEvent actionEvent) {
        if (this.getSelectedFilenameClientSide() == null && this.getSelectedFilenameServerSide() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Вы уверены что хотите удалить этот файл/папку?");
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            if (this.getSelectedFilenameClientSide() != null) {
                Path deletePath = Paths.get(this.getCurrentPathClientSide(), this.getSelectedFilenameClientSide());
                if (Files.isDirectory(deletePath)) {
                    try {
                        FileUtils.deleteDirectory(new File(deletePath.toString()));
                        this.updateClientList(Paths.get(getCurrentPathClientSide()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        Files.delete(deletePath);
                        this.updateClientList(Paths.get(this.getCurrentPathClientSide()));
                    } catch (IOException e) {
                        Alert alertFailureDelete = new Alert(Alert.AlertType.ERROR, "Не удалось удалить указанный файл", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
            }
            if (this.getSelectedFilenameServerSide() != null) {
                DeleteRequest deleteRequest = new DeleteRequest(login, password, getCurrentPathServerSide(), getSelectedFilenameServerSide());
                network.msg(deleteRequest);
            }
        }
    }

    public void copyBtn(ActionEvent actionEvent) {
        if (this.getSelectedFilenameClientSide() == null && this.getSelectedFilenameServerSide() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (this.getSelectedFilenameClientSide() != null) {
            Path copyPath = Paths.get(this.getCurrentPathClientSide(), this.getSelectedFilenameClientSide());
            try {
                if (!Files.isDirectory(copyPath)) {
                    if (Files.size(copyPath) < MB_19) {
                        copySmallFile(copyPath);
                    } else {
                        copyBigFile(copyPath);
                    }
                } else {
                    File file = new File(copyPath + "forCopy.zip");
                    createZip(copyPath, file);
                    if (Files.size(file.toPath()) < MB_19) {
                        copySmallDir(copyPath, file);
                    } else {
                        copyBigDir(file.toPath());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (this.getSelectedFilenameServerSide() != null) {
            if (!getSelectedFileTypeServerSide().equals(FileInfo.FileType.DIRECTORY)) {
                CopyRequest copyRequest = new CopyRequest(CopyRequest.CommandType.COPY_FILE_FROM_SERVER, login, password);
                copyRequest.setServerPath(getCurrentPathServerSide());
                copyRequest.setFileName(getSelectedFilenameServerSide());
                copyRequest.setClientPath(getCurrentPathClientSide());
                network.msg(copyRequest);
            }
        }
    }

    private void copySmallDir(Path copyPath, File file) throws IOException {
        CopyRequest copyRequest = new CopyRequest(CopyRequest.CommandType.COPY_DIR_FROM_CLIENT, login, password);
        copyRequest.setFileSize(FileUtils.sizeOfDirectory(copyPath.toFile()));
        copyRequest.setServerPath(this.getCurrentPathServerSide());
        copyRequest.setClientPath(this.getCurrentPathClientSide());
        copyRequest.setFileName(this.getSelectedFilenameClientSide());
        copyRequest.setData(Files.readAllBytes(file.toPath()));
        network.msg(copyRequest);
    }

    private void copySmallFile(Path copyPath) {
        try {
            CopyRequest copyRequest = new CopyRequest(CopyRequest.CommandType.COPY_FILE_FROM_CLIENT, login, password);
            copyRequest.setData(Files.readAllBytes(copyPath));
            copyRequest.setFileSize(Files.size(copyPath));
            copyRequest.setServerPath(this.getCurrentPathServerSide());
            copyRequest.setFileName(this.getSelectedFilenameClientSide());
            network.msg(copyRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void moveBtn(ActionEvent actionEvent) {
        if (this.getSelectedFilenameClientSide() == null && this.getSelectedFilenameServerSide() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (this.getSelectedFilenameClientSide() != null) {
            Path movePath = Paths.get(this.getCurrentPathClientSide(), this.getSelectedFilenameClientSide());
            if (!Files.isDirectory(movePath)) {
                try {
                    if (Files.size(movePath) < MB_19) {
                        MoveRequest moveRequest = new MoveRequest(MoveRequest.CommandType.MOVE_FILE_FROM_CLIENT, login, password);
                        moveRequest.setData(Files.readAllBytes(movePath));
                        moveRequest.setFileSize(Files.size(movePath));
                        moveRequest.setServerPath(this.getCurrentPathServerSide());
                        moveRequest.setClientPath(this.getCurrentPathClientSide());
                        moveRequest.setFileName(this.getSelectedFilenameClientSide());
                        network.msg(moveRequest);
                    } else {
                        movedBigFile(movePath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (this.getSelectedFilenameServerSide() != null) {
            if (!getSelectedFileTypeServerSide().equals(FileInfo.FileType.DIRECTORY)) {
                MoveRequest moveRequest = new MoveRequest(MoveRequest.CommandType.MOVE_FILE_FROM_SERVER, login, password);
                moveRequest.setServerPath(getCurrentPathServerSide());
                moveRequest.setFileName(getSelectedFilenameServerSide());
                moveRequest.setClientPath(getCurrentPathClientSide());
                network.msg(moveRequest);
            }
        }
    }

    private void copyBigFile(Path copyPath) throws IOException {
        byte[] data = Files.readAllBytes(copyPath);
        List<byte[]> listData = new ArrayList<>();
        int len = MB_19;
        if (data.length > MB_19) {
            int count = (int) Math.ceil((double) data.length / MB_19);
            int fin = (data.length % MB_19);
            int start_position = 0;
            int end_position = len;
            for (int i = 0; i < count; i++) {
                listData.add(Arrays.copyOfRange(data, start_position, end_position));
                start_position += len;
                if (i == count - 2) {
                    end_position = start_position + fin;
                } else {
                    end_position = start_position + len;
                }
            }
        }
        for (int i = 0; i < listData.size(); i++) {
            CopyRequest copyRequest = new CopyRequest(CopyRequest.CommandType.COPY_BIG_FILE_FROM_CLIENT, login, password);
            if (i == 0) {
                copyRequest.setFirstPartFile(true);
            }
            if (i == listData.size() - 1) {
                copyRequest.setLastPartFile(true);
                copyRequest.setFileSize((long) listData.get(i).length);
            }
            copyRequest.setData(listData.get(i));
            copyRequest.setServerPath(getCurrentPathServerSide());
            copyRequest.setFileName(getSelectedFilenameClientSide());
            network.msg(copyRequest);
        }
    }

    private void copyBigDir(Path copyPath) throws IOException {
        byte[] data = Files.readAllBytes(copyPath);
        List<byte[]> listData = new ArrayList<>();
        int len = MB_19;
        if (data.length > MB_19) {
            int count = (int) Math.ceil((double) data.length / MB_19);
            int fin = (data.length % MB_19);
            int start_position = 0;
            int end_position = len;
            for (int i = 0; i < count; i++) {
                listData.add(Arrays.copyOfRange(data, start_position, end_position));
                start_position += len;
                if (i == count - 2) {
                    end_position = start_position + fin;
                } else {
                    end_position = start_position + len;
                }
            }
        }
        for (int i = 0; i < listData.size(); i++) {
            CopyRequest copyRequest = new CopyRequest(CopyRequest.CommandType.COPY_BIG_DIR_FROM_CLIENT, login, password);
            if (i == 0) {
                copyRequest.setFirstPartFile(true);
            }
            if (i == listData.size() - 1) {
                copyRequest.setLastPartFile(true);
                copyRequest.setFileSize((long) listData.get(i).length);
                copyRequest.setClientPath(getCurrentPathClientSide());
            }
            copyRequest.setData(listData.get(i));
            copyRequest.setServerPath(getCurrentPathServerSide());
            copyRequest.setFileName(getSelectedFilenameClientSide());
            network.msg(copyRequest);
        }
    }

    private void movedBigFile(Path copyPath) throws IOException {
        byte[] data = Files.readAllBytes(copyPath);
        List<byte[]> listData = new ArrayList<>();
        int len = MB_19;
        if (data.length > MB_19) {
            int count = (int) Math.ceil((double) data.length / MB_19);
            int fin = (data.length % MB_19);
            int start_position = 0;
            int end_position = len;
            for (int i = 0; i < count; i++) {
                listData.add(Arrays.copyOfRange(data, start_position, end_position));
                start_position += len;
                if (i == count - 2) {
                    end_position = start_position + fin;
                } else {
                    end_position = start_position + len;
                }
            }
        }
        for (int i = 0; i < listData.size(); i++) {
            MoveRequest moveRequest = new MoveRequest(MoveRequest.CommandType.MOVE_BIG_FILE_FROM_CLIENT, login, password);
            if (i == 0) {
                moveRequest.setFirstPartFile(true);
            }
            if (i == listData.size() - 1) {
                moveRequest.setLastPartFile(true);
                moveRequest.setFileSize((long) listData.get(i).length);
                moveRequest.setClientPath(this.getCurrentPathClientSide());
            }
            moveRequest.setData(listData.get(i));
            moveRequest.setServerPath(getCurrentPathServerSide());
            moveRequest.setFileName(getSelectedFilenameClientSide());
            network.msg(moveRequest);
        }
    }

    private void createZip(Path copyPath, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(String.valueOf(copyPath));
            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public void btnNewDir(ActionEvent actionEvent) {
        if (newDirField.getText() != null && !newDirField.getText().trim().equals("") && !newDirField.getText().trim().equals(" ")) {
            CreateDirRequest createDirRequest = new CreateDirRequest(login, password, newDirField.getText(), pathFieldServer.getText());
            newDirField.setText("");
            network.msg(createDirRequest);
        }
    }

    public void setFreeSpaseField(String freeSpaseField) {
        this.freeSpaseField.setText(freeSpaseField);
    }
}