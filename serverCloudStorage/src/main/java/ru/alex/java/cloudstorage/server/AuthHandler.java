package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import ru.alex.java.cloudstorage.common.AuthRequest;
import ru.alex.java.cloudstorage.common.AuthResponse;
import ru.alex.java.cloudstorage.common.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public AuthHandler(ServiceDb serviceDb) {
        this.serviceDb=serviceDb;
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
                authResponse.setFreeSpaseStorage(getFreeSpace(request));
                authResponse.setLogin(login);
                authResponse.setFileInfoList(enrichFileInfoList(getFullNamePath(login)));
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

    private List<FileInfo> enrichFileInfoList(String pathServerList) throws IOException {
        try (Stream<Path> list = Files.list(Path.of(pathServerList))){
            return  list.map(FileInfo::new)
                    .collect(Collectors.toList());
        }
    }

    public String getFullNamePath(String pathFromServer) {
        return ROOT.resolve(pathFromServer).toString();
    }
    public String getFreeSpace(AuthRequest request) {
        long diskSpaceUsed = FileUtils.sizeOfDirectory(new File(getFullNamePath(request.getLogin())));
          return String.valueOf((serviceDb.getDiskQuota(request.getLogin())-diskSpaceUsed)/1048576).concat(" MB");
    }
}