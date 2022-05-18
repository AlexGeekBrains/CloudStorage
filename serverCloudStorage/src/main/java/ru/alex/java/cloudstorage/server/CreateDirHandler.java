package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import ru.alex.java.cloudstorage.common.CreateDirRequest;
import ru.alex.java.cloudstorage.common.CreateDirResponse;
import ru.alex.java.cloudstorage.common.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateDirHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public CreateDirHandler(ServiceDb serviceDb) {
        this.serviceDb=serviceDb;
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
                    response.setFileInfoList(enrichFileInfoList(getFullNamePath(request.getServerPath())));
                    response.setServerPath(request.getServerPath());
                    response.setFreeSpaseStorage(getFreeSpace(request));
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
        return Path.of(getFullNamePath(request.getServerPath().concat("/").concat(request.getDirName())));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    public String getFullNamePath(String pathFromServer) {
        return ROOT.resolve(pathFromServer).toString();
    }

    private List<FileInfo> enrichFileInfoList(String pathServerList) throws IOException {
        try (Stream<Path> list = Files.list(Path.of(pathServerList))) {
            return list.map(FileInfo::new)
                    .collect(Collectors.toList());
        }
    }
    public String getFreeSpace(CreateDirRequest request) {
        long diskSpaceUsed = FileUtils.sizeOfDirectory(new File(getFullNamePath(request.getLogin())));
        return String.valueOf((serviceDb.getDiskQuota(request.getLogin())-diskSpaceUsed)/1048576).concat(" MB");
    }
}