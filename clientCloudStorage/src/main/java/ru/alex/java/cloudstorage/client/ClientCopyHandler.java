package ru.alex.java.cloudstorage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import ru.alex.java.cloudstorage.common.CopyResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.alex.java.cloudstorage.common.CopyResponse.CommandType.*;

public class ClientCopyHandler extends ChannelInboundHandlerAdapter {
    private CloudStorageController controller;

    public ClientCopyHandler(CloudStorageController controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof CopyResponse) {
            CopyResponse serverAnswer = (CopyResponse) msg;
            if (COPY_FILE_FROM_CLIENT.equals(serverAnswer.getCommand()) || COPY_BIG_FILE_FROM_CLIENT.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
                });
            }
            if (COPY_DIR_FROM_CLIENT.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    try {
                        Files.delete(Path.of(serverAnswer.getClientPath(), serverAnswer.getFileName() + "forCopy.zip"));
                        controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            if (COPY_FILE_FROM_SERVER.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    try {
                        Files.write(Path.of(serverAnswer.getClientPath().concat("/").concat(serverAnswer.getFileName())), serverAnswer.getData());
                        controller.updateClientList(Path.of(serverAnswer.getClientPath()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            if (COPY_BIG_FILE_FROM_SERVER.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    File file = new File(serverAnswer.getClientPath(), serverAnswer.getFileName());
                    if (serverAnswer.getData() != null) {
                        try {
                            if (serverAnswer.isFirstPartFile()) {
                                FileUtils.writeByteArrayToFile(file, serverAnswer.getData());
                            } else if (serverAnswer.isLastPartFile()) {
                                FileUtils.writeByteArrayToFile(file, serverAnswer.getData(), true);
                                controller.updateClientList(Path.of(serverAnswer.getClientPath()));
                            } else {
                                FileUtils.writeByteArrayToFile(file, serverAnswer.getData(), true);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
            if (NO_COPY_FROM_CLIENT.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    if (serverAnswer.getFileName() != null) {
                        try {
                            Files.delete(Path.of(serverAnswer.getClientPath(), serverAnswer.getFileName() + "forCopy.zip"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
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