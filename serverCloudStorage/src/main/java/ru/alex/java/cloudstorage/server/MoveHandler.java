package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import ru.alex.java.cloudstorage.common.FileInfo;
import ru.alex.java.cloudstorage.common.MoveRequest;
import ru.alex.java.cloudstorage.common.MoveResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.alex.java.cloudstorage.common.MoveRequest.CommandType.*;

public class MoveHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private static final int MB_19 = 19 * 1_000_000;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public MoveHandler() {
        serviceDb = new SqliteServiceDb();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MoveRequest) {
            MoveRequest request = (MoveRequest) msg;
            if (MOVE_FILE_FROM_CLIENT.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    if (checkFreeSpace(request)) {
                        FileUtils.writeByteArrayToFile(new File(getFullNamePathForCopy(request.getServerPath(), request.getFileName())), request.getData());
                        MoveResponse response = new MoveResponse(MoveResponse.CommandType.MOVE_FILE_FROM_CLIENT);
                        response.setClientPath(request.getClientPath());
                        response.setFileName(request.getFileName());
                        response.setServerPath(request.getServerPath());
                        updateFileInfoList(response, getFullNamePath(request.getServerPath()));
                        ctx.writeAndFlush(response);
                    } else {
                        MoveResponse response = new MoveResponse(MoveResponse.CommandType.NO_MOVE_FILE_FROM_CLIENT);
                        ctx.writeAndFlush(response);
                    }
                }
            } else if (MOVE_FILE_FROM_SERVER.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    Path movePath = Paths.get(getFullNamePathForCopy(request.getServerPath(), request.getFileName()));
                    if (Files.size(movePath) < MB_19) {
                        MoveResponse response = new MoveResponse(MoveResponse.CommandType.MOVE_FILE_FROM_SERVER);
                        response.setClientPath(request.getClientPath());
                        response.setFileName(request.getFileName());
                        response.setServerPath(request.getServerPath());
                        response.setData(Files.readAllBytes(movePath));
                        Files.delete(movePath);
                        updateFileInfoList(response, getFullNamePath(request.getServerPath()));
                        ctx.writeAndFlush(response);
                    } else {
                        moveBigFile(ctx, request, movePath);
                    }
                }
            } else if (MOVE_BIG_FILE_FROM_CLIENT.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    File file = new File((getFullNamePathForCopy(request.getServerPath(), request.getFileName())));
                    if (request.isFirstPartFile()) {
                        FileUtils.writeByteArrayToFile(file, request.getData());
                    } else if (request.isLastPartFile()) {
                        if (checkFreeSpace(request)) {
                            FileUtils.writeByteArrayToFile(file, request.getData(), true);
                            MoveResponse moveResponse = new MoveResponse(MoveResponse.CommandType.MOVE_BIG_FILE_FROM_CLIENT);
                            moveResponse.setClientPath(request.getClientPath());
                            moveResponse.setServerPath(request.getServerPath());
                            moveResponse.setFileName(request.getFileName());
                            updateFileInfoList(moveResponse, getFullNamePath(request.getServerPath()));
                            ctx.writeAndFlush(moveResponse);
                        } else {
                            Files.delete(file.toPath());
                            MoveResponse moveResponse = new MoveResponse(MoveResponse.CommandType.NO_MOVE_FILE_FROM_CLIENT);
                            ctx.writeAndFlush(moveResponse);
                        }
                    } else FileUtils.writeByteArrayToFile(file, request.getData(), true);
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

    private void updateFileInfoList(MoveResponse response, String pathServerList) throws IOException {
        List<FileInfo> serverList = Files.list(Path.of(pathServerList))
                .map(FileInfo::new)
                .collect(Collectors.toList());
        response.setFileInfoList(serverList);
    }

    public String getFullNamePath(String pathFromServer) {
        return ROOT.resolve(pathFromServer).toString();
    }

    public String getFullNamePathForCopy(String pathFromServer, String fileNameFromClient) {
        return ROOT.resolve(pathFromServer).resolve(fileNameFromClient).toString();
    }

    private void moveBigFile(ChannelHandlerContext ctx, MoveRequest request, Path movePath) throws IOException {
        byte[] data = Files.readAllBytes(movePath);
        List<byte[]> listData = new ArrayList<>();
        int len = MB_19;
        if (data.length > MB_19) {
            int count = (int) Math.ceil((double) data.length / MB_19);
            int fin = (data.length % MB_19);
            int start_position = 0;
            int end_position = len;
            for (int i = 0; i < count; i++) {
                listData.add(Arrays.copyOfRange(data, start_position, end_position));
                start_position += len;
                if (i == count - 2) {
                    end_position = start_position + fin;
                } else {
                    end_position = start_position + len;
                }
            }
        }
        MoveResponse moveResponseFirstPart = new MoveResponse(MoveResponse.CommandType.MOVE_BIG_FILE_FROM_SERVER);
        moveResponseFirstPart.setClientPath(request.getClientPath());
        moveResponseFirstPart.setFileName(request.getFileName());
        ctx.writeAndFlush(moveResponseFirstPart);
        for (int i = 0; i < listData.size(); i++) {
            MoveResponse moveResponse = new MoveResponse(MoveResponse.CommandType.MOVE_BIG_FILE_FROM_SERVER);
            if (i == 0) {
                moveResponse.setFirstPartFile(true);
                moveResponse.setData(listData.get(i));
                moveResponse.setClientPath(request.getClientPath());
                moveResponse.setFileName(request.getFileName());
                ctx.writeAndFlush(moveResponse);
            } else if (i == listData.size() - 1) {
                moveResponse.setLastPartFile(true);
                moveResponse.setData(listData.get(i));
                moveResponse.setClientPath(request.getClientPath());
                moveResponse.setServerPath(request.getServerPath());
                moveResponse.setFileName(request.getFileName());
                Files.delete(movePath);
                updateFileInfoList(moveResponse, getFullNamePath(request.getServerPath()));
                ctx.writeAndFlush(moveResponse);
            } else {
                moveResponse.setData(listData.get(i));
                moveResponse.setClientPath(request.getClientPath());
                moveResponse.setFileName(request.getFileName());
                ctx.writeAndFlush(moveResponse);
            }
        }
    }
    public boolean checkFreeSpace(MoveRequest request) {
        long diskSpaceUsed = FileUtils.sizeOfDirectory(new File(getFullNamePath(request.getLogin())));
        return diskSpaceUsed + request.getFileSize() < serviceDb.getDiskQuota(request.getLogin());
    }
}