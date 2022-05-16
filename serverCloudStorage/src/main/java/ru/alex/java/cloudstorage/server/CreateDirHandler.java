package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.alex.java.cloudstorage.common.CreateDirRequest;
import ru.alex.java.cloudstorage.common.CreateDirResponse;
import ru.alex.java.cloudstorage.common.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CreateDirHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public CreateDirHandler() {
        serviceDb = new SqliteServiceDb();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof CreateDirRequest) {
            CreateDirRequest request = (CreateDirRequest) msg;
            CreateDirResponse response = new CreateDirResponse();
            if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                Files.createDirectory(getPathNewDir(request));
                if (getNesting(request) <= serviceDb.getMaxNesting(request.getLogin())) {
                    response.setCommand(CreateDirResponse.CommandType.CREATE_DIR_OK);
                    updateFileInfoList(response, getFullNamePath(request.getServerPath()));
                    response.setServerPath(request.getServerPath());
                    ctx.writeAndFlush(response);
                } else {
                    Files.deleteIfExists(getPathNewDir(request));
                    response.setCommand(CreateDirResponse.CommandType.CREATE_DIR_NO);
                    ctx.writeAndFlush(response);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private int getNesting(CreateDirRequest request) {
        int path = getPathNewDir(request).getNameCount() - Path.of(getFullNamePath(request.getLogin())).getNameCount();
        return path;
    }

    private Path getPathNewDir(CreateDirRequest request) {
        Path newDir;
        return newDir = Path.of(getFullNamePath(request.getServerPath().concat("/").concat(request.getDirName())));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    public String getFullNamePath(String pathFromServer) {
        return ROOT.resolve(pathFromServer).toString();
    }

    private void updateFileInfoList(CreateDirResponse response, String pathServerList) throws IOException {
        List<FileInfo> serverList = Files.list(Path.of(pathServerList))
                .map(FileInfo::new)
                .collect(Collectors.toList());
        response.setFileInfoList(serverList);
    }
}