package ru.alex.java.cloudstorage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import ru.alex.java.cloudstorage.common.RegResponse;

import static ru.alex.java.cloudstorage.common.RegResponse.CommandType.REG_NO;
import static ru.alex.java.cloudstorage.common.RegResponse.CommandType.REG_OK;

public class ClientRegHandler extends ChannelInboundHandlerAdapter {
    private CloudStorageController controller;

    public ClientRegHandler(CloudStorageController controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RegResponse) {
            RegResponse serverAnswer = (RegResponse) msg;
            if (REG_OK.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.regAlertSuccess();
                });
            }
            if (REG_NO.equals(serverAnswer.getCommand())) {
                Platform.runLater(() -> {
                    controller.regAlertFailure();
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