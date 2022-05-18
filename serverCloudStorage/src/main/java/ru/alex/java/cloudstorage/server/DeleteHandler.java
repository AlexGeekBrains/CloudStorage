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
import java.util.stream.Stream;

public class DeleteHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public DeleteHandler(ServiceDb serviceDb) {
        this.serviceDb = serviceDb;
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
                    response.setFileInfoList(enrichFileInfoList(getFullNamePath(request.getServerPath())));
                    response.setCommand(DeleteResponse.CommandType.DELETE_OK);
                    response.setServerPath(request.getServerPath());
                    response.setFreeSpaseStorage(getFreeSpace(request));
                    ctx.writeAndFlush(response);
                } else {
                    try {
                        Files.deleteIfExists(deletePath);
                        response.setFileInfoList(enrichFileInfoList(getFullNamePath(request.getServerPath())));
                        response.setCommand(DeleteResponse.CommandType.DELETE_OK);
                        response.setServerPath(request.getServerPath());
                        response.setFreeSpaseStorage(getFreeSpace(request));
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

    private List<FileInfo> enrichFileInfoList(String pathServerList) throws IOException {
        try (Stream<Path> list = Files.list(Path.of(pathServerList))) {
            return list.map(FileInfo::new)
                    .collect(Collectors.toList());
        }
    }

    public String getFullNamePath(String pathFromServer) {
        return ROOT.resolve(pathFromServer).toString();
    }

    public String getFreeSpace(DeleteRequest request) {
        long diskSpaceUsed = FileUtils.sizeOfDirectory(new File(getFullNamePath(request.getLogin())));
        return String.valueOf((serviceDb.getDiskQuota(request.getLogin()) - diskSpaceUsed) / 1048576).concat(" MB");
    }
}