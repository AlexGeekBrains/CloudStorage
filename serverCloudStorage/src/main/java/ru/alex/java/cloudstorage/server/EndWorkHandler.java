package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.alex.java.cloudstorage.common.EndWorkRequest;

public class EndWorkHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;

    public EndWorkHandler(ServiceDb serviceDb) {
        this.serviceDb=serviceDb;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof EndWorkRequest) {
            EndWorkRequest request = (EndWorkRequest) msg;
            if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())){
                ctx.close();
            }
        }
    }
}