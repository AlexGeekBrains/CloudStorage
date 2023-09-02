package ru.alex.java.cloudstorage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class ClientNetwork {
    private static final int PORT = 45001;
    private static final String HOST = "localhost";
    private Channel channel;
    public static final int MB_20 = 20 * 1_000_000;

    public ClientNetwork(CloudStorageController controller) {
        new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast(
                                        new ObjectDecoder(MB_20, ClassResolvers.cacheDisabled(null)),
                                        new ObjectEncoder(),
                                        new ClientRegHandler(controller),
                                        new ClientAuthHandler(controller),
                                        new ClientTransitionHandler(controller),
                                        new ClientDeleteHandler(controller),
                                        new ClientCopyHandler(controller),
                                        new ClientCreateDirHandler(controller),
                                        new ClientMoveHandler(controller));
                            }
                        });
                ChannelFuture channelFuture = b.connect(HOST, PORT).sync();
                channel = channelFuture.channel();
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        }).start();
    }

    public void msg(Object obj) {
        channel.writeAndFlush(obj);
    }
}