package ru.alex.java.cloudstorage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.sql.SQLException;

public class Server {
    private static final int PORT = 45001;
    private static final String HOST = "localhost";
    private static final int MB_20 = 20 * 1_000_000;
    private static ServiceDb serviceDb;

    public static void main(String[] args) throws InterruptedException {
        try {
            serviceDb = new SqliteServiceDb();
            DataSource.connect();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline inbound = socketChannel.pipeline();
                            inbound.addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(MB_20, ClassResolvers.cacheDisabled(null)),
                                    new RegHandler(serviceDb),
                                    new AuthHandler(serviceDb),
                                    new DeleteHandler(serviceDb),
                                    new TransitionPathHandler(serviceDb),
                                    new CopyHandler(serviceDb),
                                    new CreateDirHandler(serviceDb),
                                    new MoveHandler(serviceDb),
                                    new EndWorkHandler(serviceDb)
                            );
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(HOST, PORT).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            DataSource.disconnect();
        }
    }
}