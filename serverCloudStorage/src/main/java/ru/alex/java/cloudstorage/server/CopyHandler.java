package ru.alex.java.cloudstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import ru.alex.java.cloudstorage.common.CopyRequest;
import ru.alex.java.cloudstorage.common.CopyResponse;
import ru.alex.java.cloudstorage.common.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static ru.alex.java.cloudstorage.common.CopyRequest.CommandType.*;

public class CopyHandler extends ChannelInboundHandlerAdapter {
    private ServiceDb serviceDb;
    private static final int MB_19 = 19 * 1_000_000;
    private final static Path ROOT = Paths.get("serverCloudStorage/directoryServer");

    public CopyHandler(ServiceDb serviceDb) {
        this.serviceDb=serviceDb;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof CopyRequest) {
            CopyRequest request = (CopyRequest) msg;
            if (COPY_FILE_FROM_CLIENT.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    if (checkFreeSpace(request)) {
                        FileUtils.writeByteArrayToFile(new File(getFullNamePathWithFileName(request.getServerPath(), request.getFileName())), request.getData());
                        CopyResponse response = new CopyResponse(CopyResponse.CommandType.COPY_FILE_FROM_CLIENT);
                        response.setServerPath(request.getServerPath());
                        response.setFileInfoList(enrichFileInfoList(getFullNamePath(request.getServerPath())));
                        response.setFreeSpaseStorage(getFreeSpace(request));
                        ctx.writeAndFlush(response);
                    } else {
                        CopyResponse response = new CopyResponse(CopyResponse.CommandType.NO_COPY_FROM_CLIENT);
                        ctx.writeAndFlush(response);
                    }
                }
            } else if (COPY_FILE_FROM_SERVER.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    Path copyPath = Paths.get(getFullNamePathWithFileName(request.getServerPath(), request.getFileName()));
                    CopyResponse response = new CopyResponse(CopyResponse.CommandType.COPY_FILE_FROM_SERVER);
                    response.setClientPath(request.getClientPath());
                    response.setFileName(request.getFileName());
                    if (Files.size(copyPath) < MB_19) {
                        response.setData(Files.readAllBytes(copyPath));
                        ctx.writeAndFlush(response);
                    } else {
                        copyBigFile(ctx, request, copyPath);
                    }
                }
            } else if (COPY_BIG_FILE_FROM_CLIENT.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    File file = new File((getFullNamePathWithFileName(request.getServerPath(), request.getFileName())));
                    if (request.isFirstPartFile()) {
                        FileUtils.writeByteArrayToFile(file, request.getData());
                    } else if (request.isLastPartFile()) {
                        if (checkFreeSpace(request)) {
                            FileUtils.writeByteArrayToFile(file, request.getData(), true);
                            CopyResponse copyResponse = new CopyResponse(CopyResponse.CommandType.COPY_BIG_FILE_FROM_CLIENT);
                            copyResponse.setServerPath(request.getServerPath());
                            copyResponse.setFileInfoList(enrichFileInfoList(getFullNamePath(request.getServerPath())));
                            copyResponse.setFreeSpaseStorage(getFreeSpace(request));
                            ctx.writeAndFlush(copyResponse);
                        } else {
                            Files.delete(file.toPath());
                            CopyResponse response = new CopyResponse(CopyResponse.CommandType.NO_COPY_FROM_CLIENT);
                            ctx.writeAndFlush(response);
                        }
                    } else FileUtils.writeByteArrayToFile(file, request.getData(), true);
                }
            } else if (COPY_DIR_FROM_CLIENT.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    if (checkFreeSpace(request)) {
                        FileUtils.writeByteArrayToFile(new File(getFullNamePathWithFileName(request.getServerPath(), request.getFileName() + ".zip")), request.getData());
                        unpackAndResponse(ctx, request);
                    } else {
                        copyFailureNotEnoughSpace(ctx, request);
                    }
                }
            } else if (COPY_BIG_DIR_FROM_CLIENT.equals(request.getCommand())) {
                if (serviceDb.isAuthentication(request.getLogin(), request.getPassword())) {
                    File file = new File((getFullNamePathWithFileName(request.getServerPath(), request.getFileName() + ".zip")));
                    if (request.isFirstPartFile()) {
                        FileUtils.writeByteArrayToFile(file, request.getData());
                    } else if (request.isLastPartFile()) {
                        if (checkFreeSpace(request)) {
                            FileUtils.writeByteArrayToFile(file, request.getData(), true);
                            unpackAndResponse(ctx, request);
                        } else {
                            Files.delete(file.toPath());
                            copyFailureNotEnoughSpace(ctx, request);
                        }
                    } else FileUtils.writeByteArrayToFile(file, request.getData(), true);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void copyFailureNotEnoughSpace(ChannelHandlerContext ctx, CopyRequest request) {
        CopyResponse response = new CopyResponse(CopyResponse.CommandType.NO_COPY_FROM_CLIENT);
        response.setClientPath(request.getClientPath());
        response.setFileName(request.getFileName());
        ctx.writeAndFlush(response);
    }

    private void unpackAndResponse(ChannelHandlerContext ctx, CopyRequest request) throws IOException {
        String fileZip = getFullNamePathWithFileName(request.getServerPath(), request.getFileName() + ".zip");
        unpackZip(request, fileZip);
        Files.delete(Path.of(getFullNamePathWithFileName(request.getServerPath(), request.getFileName()) + ".zip"));
        CopyResponse copyResponse = new CopyResponse(CopyResponse.CommandType.COPY_DIR_FROM_CLIENT);
        copyResponse.setServerPath(request.getServerPath());
        copyResponse.setFileName(request.getFileName());
        copyResponse.setClientPath(request.getClientPath());
        copyResponse.setFileInfoList(enrichFileInfoList(getFullNamePath(request.getServerPath())));
        copyResponse.setFreeSpaseStorage(getFreeSpace(request));
        ctx.writeAndFlush(copyResponse);
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

    public String getFullNamePathWithFileName(String pathFromServer, String fileNameFromClient) {
        return ROOT.resolve(pathFromServer).resolve(fileNameFromClient).toString();
    }

    private void copyBigFile(ChannelHandlerContext ctx, CopyRequest request, Path copyPath) throws IOException {
        byte[] data = Files.readAllBytes(copyPath);
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
        CopyResponse copyResponseFirstPart = new CopyResponse(CopyResponse.CommandType.COPY_BIG_FILE_FROM_SERVER);
        copyResponseFirstPart.setClientPath(request.getClientPath());
        copyResponseFirstPart.setFileName(request.getFileName());
        ctx.writeAndFlush(copyResponseFirstPart);
        for (int i = 0; i < listData.size(); i++) {
            CopyResponse copyResponse = new CopyResponse(CopyResponse.CommandType.COPY_BIG_FILE_FROM_SERVER);
            if (i == 0) {
                copyResponse.setFirstPartFile(true);
            }
            if (i == listData.size() - 1) {
                copyResponse.setLastPartFile(true);
            }
            copyResponse.setData(listData.get(i));
            copyResponse.setClientPath(request.getClientPath());
            copyResponse.setFileName(request.getFileName());
            ctx.writeAndFlush(copyResponse);
        }
    }

    private void unpackZip(CopyRequest request, String fileZip) throws IOException {
        File destDir = new File(getFullNamePath(request.getServerPath()));
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = new File(destDir, String.valueOf(zipEntry));
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    public boolean checkFreeSpace(CopyRequest request) {
        long diskSpaceUsed = FileUtils.sizeOfDirectory(new File(getFullNamePath(request.getLogin())));
        return diskSpaceUsed + request.getFileSize() < serviceDb.getDiskQuota(request.getLogin());
    }
    public String getFreeSpace(CopyRequest request) {
        long diskSpaceUsed = FileUtils.sizeOfDirectory(new File(getFullNamePath(request.getLogin())));
        return String.valueOf((serviceDb.getDiskQuota(request.getLogin())-diskSpaceUsed)/1048576).concat(" MB");
    }
}