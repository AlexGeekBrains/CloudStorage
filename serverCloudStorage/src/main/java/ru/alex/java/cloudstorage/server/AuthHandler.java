package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.alex.java.cloudstorage.common.AuthRequest;
import ru.alex.java.cloudstorage.common.AuthResponse;
import ru.alex.java.cloudstorage.common.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public AuthHandler() {
        serviceDb = new SqliteServiceDb();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof AuthRequest) {
            AuthRequest request = (AuthRequest) msg;
            AuthResponse authResponse = new AuthResponse();
            if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                String login = serviceDb.getLoginByLoginAndPassword(request.getLogin(), request.getPassword());
                System.out.println("Client: " + login + " authenticated");
                authResponse.setCommand(AuthResponse.CommandType.AUTH_OK);
                authResponse.setLogin(login);
                updateFileInfoList(authResponse, getFullNamePath(login));
                authResponse.setServerPath(login);
            } else {
                authResponse.setCommand(AuthResponse.CommandType.AUTH_NO);
            }
            ctx.writeAndFlush(authResponse);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private void updateFileInfoList(AuthResponse authResponse, String pathServerList) throws IOException {
        List<FileInfo> serverList = Files.list(Path.of(pathServerList))
                .map(FileInfo::new)
                .collect(Collectors.toList());
        authResponse.setFileInfoList(serverList);
    }

    public String getFullNamePath(String pathFromServer) {
        return ROOT.resolve(pathFromServer).toString();
    }
}