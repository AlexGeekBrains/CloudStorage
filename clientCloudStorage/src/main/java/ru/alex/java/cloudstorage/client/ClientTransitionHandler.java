package ru.alex.java.cloudstorage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import ru.alex.java.cloudstorage.common.TransitionResponse;

public class ClientTransitionHandler extends ChannelInboundHandlerAdapter {
    private CloudStorageController controller;

    public ClientTransitionHandler(CloudStorageController controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TransitionResponse) {
            TransitionResponse serverAnswer = (TransitionResponse) msg;
            Platform.runLater(() -> {
                controller.updateServerList(serverAnswer.getServerPath(), serverAnswer.getFileInfoList());
            });
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}