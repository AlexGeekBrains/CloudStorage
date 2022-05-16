package ru.alex.java.cloudstorage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import ru.alex.java.cloudstorage.common.CreateDirResponse;

import static ru.alex.java.cloudstorage.common.CreateDirResponse.CommandType.CREATE_DIR_NO;
import static ru.alex.java.cloudstorage.common.CreateDirResponse.CommandType.CREATE_DIR_OK;

public class ClientCreateDirHandler extends ChannelInboundHandlerAdapter {
    private CloudStorageController controller;

    public ClientCreateDirHandler(CloudStorageController controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof CreateDirResponse) {
            CreateDirResponse serverAnswer = (CreateDirResponse) msg;
            if (CREATE_DIR_OK.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
                });
            }
            if (CREATE_DIR_NO.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.createNewDirFailure();
                });
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}