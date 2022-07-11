package com.tsuki.fseslconnector;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class FsEslClient implements Runnable {

    ClientHandler clientHandler;

    public FsEslClient(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
        new Thread(this).start();
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();

            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress("10.10.100.1", 8021));

            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE, true, false));
                    socketChannel.pipeline().addLast(new ClientHandler());
                }
            });
            ChannelFuture channelFuture;
            try {
                channelFuture = clientBootstrap.connect().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            try {
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
