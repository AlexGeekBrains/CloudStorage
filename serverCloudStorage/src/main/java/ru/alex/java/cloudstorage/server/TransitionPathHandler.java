package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.alex.java.cloudstorage.common.FileInfo;
import ru.alex.java.cloudstorage.common.TransitionRequest;
import ru.alex.java.cloudstorage.common.TransitionResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static ru.alex.java.cloudstorage.common.TransitionRequest.CommandType.NEXT_PATH;
import static ru.alex.java.cloudstorage.common.TransitionRequest.CommandType.UP_PATH;

public class TransitionPathHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public TransitionPathHandler() {
        serviceDb = new SqliteServiceDb();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TransitionRequest) {
            TransitionRequest request = (TransitionRequest) msg;
            TransitionResponse response = new TransitionResponse();
            if (NEXT_PATH.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    updateFileInfoList(response, getFullNamePath(request.getServerPath()));
                    response.setServerPath(request.getServerPath());
                    ctx.writeAndFlush(response);
                }
            } else if (UP_PATH.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    updateFileInfoList(response, getFullNamePath(request.getServerPath()));
                    response.setServerPath(request.getServerPath());
                    ctx.writeAndFlush(response);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private void updateFileInfoList(TransitionResponse response, String pathServerList) throws IOException {
        List<FileInfo> serverList = Files.list(Path.of(pathServerList))
                .map(FileInfo::new)
                .collect(Collectors.toList());
        response.setFileInfoList(serverList);
    }

    public String getFullNamePath(String pathFromServer) {
        return ROOT.resolve(pathFromServer).toString();
    }
}