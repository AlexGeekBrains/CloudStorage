package ru.alex.java.cloudstorage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import ru.alex.java.cloudstorage.common.AuthResponse;

import static ru.alex.java.cloudstorage.common.AuthResponse.CommandType.AUTH_NO;
import static ru.alex.java.cloudstorage.common.AuthResponse.CommandType.AUTH_OK;

public class ClientAuthHandler extends ChannelInboundHandlerAdapter {
    private CloudStorageController controller;

    public ClientAuthHandler(CloudStorageController controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof AuthResponse) {
            AuthResponse serverAnswer = (AuthResponse) msg;
            if (AUTH_OK.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.showWorkArea();
                    controller.setFreeSpaseField(serverAnswer.getFreeSpaseStorage());
                    controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
                    controller.setTitle(serverAnswer.getLogin());
                });
            }
            if (AUTH_NO.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.authAlert();
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