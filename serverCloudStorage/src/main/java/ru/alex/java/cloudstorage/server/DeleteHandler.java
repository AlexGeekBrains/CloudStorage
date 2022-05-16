package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import ru.alex.java.cloudstorage.common.DeleteRequest;
import ru.alex.java.cloudstorage.common.DeleteResponse;
import ru.alex.java.cloudstorage.common.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public DeleteHandler() {
        serviceDb = new SqliteServiceDb();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DeleteRequest) {
            DeleteRequest request = (DeleteRequest) msg;
            if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                DeleteResponse response = new DeleteResponse();
                Path deletePath = Paths.get(getFullNamePath(request.getServerPath()), request.getFileName());
                if (Files.isDirectory(deletePath)) {
                    FileUtils.deleteDirectory(new File(deletePath.toString()));
                    updateFileInfoList(response, getFullNamePath(request.getServerPath()));
                    response.setCommand(DeleteResponse.CommandType.DELETE_OK);
                    response.setServerPath(request.getServerPath());
                    ctx.writeAndFlush(response);
                } else {
                    try {
                        Files.delete(deletePath);
                        updateFileInfoList(response, getFullNamePath(request.getServerPath()));
                        response.setCommand(DeleteResponse.CommandType.DELETE_OK);
                        response.setServerPath(request.getServerPath());
                        ctx.writeAndFlush(response);
                    } catch (IOException e) {
                        response.setCommand(DeleteResponse.CommandType.DELETE_NO);
                        ctx.writeAndFlush(response);
                    }
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

    private void updateFileInfoList(DeleteResponse response, String pathServerList) throws IOException {
        List<FileInfo> serverList = Files.list(Path.of(pathServerList))
                .map(FileInfo::new)
                .collect(Collectors.toList());
        response.setFileInfoList(serverList);
    }

    public String getFullNamePath(String pathFromServer) {
        return ROOT.resolve(pathFromServer).toString();
    }
}