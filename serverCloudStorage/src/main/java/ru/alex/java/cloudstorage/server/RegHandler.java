package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.alex.java.cloudstorage.common.RegRequest;
import ru.alex.java.cloudstorage.common.RegResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RegHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public RegHandler() {
        serviceDb = new SqliteServiceDb();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RegRequest) {
            RegRequest request = (RegRequest) msg;
            RegResponse regResponse = new RegResponse();
            if (serviceDb.isRegistration(request.getLogin(), request.getPassword())) {
                if (!Files.exists(Paths.get(getFullNamePath(request.getLogin())))) {
                    Files.createDirectory(Paths.get(getFullNamePath(request.getLogin())));
                }
                regResponse.setCommand(RegResponse.CommandType.REG_OK);
            } else {
                regResponse.setCommand(RegResponse.CommandType.REG_NO);
            }
            ctx.writeAndFlush(regResponse);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    public String getFullNamePath(String pathFromServer) {
        return ROOT.resolve(pathFromServer).toString();
    }
}