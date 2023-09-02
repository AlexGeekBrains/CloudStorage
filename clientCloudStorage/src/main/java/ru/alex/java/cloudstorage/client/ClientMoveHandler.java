package ru.alex.java.cloudstorage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import ru.alex.java.cloudstorage.common.MoveResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.alex.java.cloudstorage.common.MoveResponse.CommandType.*;

public class ClientMoveHandler extends ChannelInboundHandlerAdapter {
    private CloudStorageController controller;

    public ClientMoveHandler(CloudStorageController controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MoveResponse) {
            MoveResponse serverAnswer = (MoveResponse) msg;
            if (MOVE_FILE_FROM_CLIENT.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    try {
                        Files.deleteIfExists(Path.of(serverAnswer.getClientPath().concat("/").concat(serverAnswer.getFileName())));
                        controller.setFreeSpaseField(serverAnswer.getFreeSpaseStorage());
                        controller.updateClientList(Path.of(serverAnswer.getClientPath()));
                        controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            if (MOVE_FILE_FROM_SERVER.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    try {
                        Files.write(Path.of(serverAnswer.getClientPath().concat("/").concat(serverAnswer.getFileName())), serverAnswer.getData());
                        controller.setFreeSpaseField(serverAnswer.getFreeSpaseStorage());
                        controller.updateClientList(Path.of(serverAnswer.getClientPath()));
                        controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            if (MOVE_BIG_FILE_FROM_CLIENT.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    try {
                        Files.deleteIfExists(Path.of(serverAnswer.getClientPath().concat("/").concat(serverAnswer.getFileName())));
                        controller.updateClientList(Path.of(serverAnswer.getClientPath()));
                        controller.setFreeSpaseField(serverAnswer.getFreeSpaseStorage());
                        controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            if (MOVE_BIG_FILE_FROM_SERVER.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    try {
                        File file = new File(serverAnswer.getClientPath(), serverAnswer.getFileName());
                        if (serverAnswer.getData() != null) {
                            if (serverAnswer.isFirstPartFile()) {
                                FileUtils.writeByteArrayToFile(file, serverAnswer.getData());
                            } else if (serverAnswer.isLastPartFile()) {
                                FileUtils.writeByteArrayToFile(file, serverAnswer.getData(), true);
                                controller.setFreeSpaseField(serverAnswer.getFreeSpaseStorage());
                                controller.updateClientList(Path.of(serverAnswer.getClientPath()));
                                controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
                            } else FileUtils.writeByteArrayToFile(file, serverAnswer.getData(), true);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            if (NO_MOVE_FILE_FROM_CLIENT.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.copyAlertFailure();
                });
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}