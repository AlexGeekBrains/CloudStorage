package ru.alex.java.cloudstorage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import ru.alex.java.cloudstorage.common.DeleteResponse;

import static ru.alex.java.cloudstorage.common.DeleteResponse.CommandType.DELETE_NO;
import static ru.alex.java.cloudstorage.common.DeleteResponse.CommandType.DELETE_OK;

public class ClientDeleteHandler extends ChannelInboundHandlerAdapter {
    private CloudStorageController controller;

    public ClientDeleteHandler(CloudStorageController controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DeleteResponse) {
            DeleteResponse serverAnswer = (DeleteResponse) msg;
            if (DELETE_OK.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
                });
            }
            if (DELETE_NO.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.delAlertFailure();
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