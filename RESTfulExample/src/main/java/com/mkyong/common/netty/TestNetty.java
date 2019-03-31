package com.mkyong.common.netty;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TestNetty {
	public static void main(String[] args) {
		EventLoopGroup group = new NioEventLoopGroup();
		try{
			for (int i = 0; i < 2; i ++) {
				Bootstrap clientBootstrap = new Bootstrap();

				clientBootstrap.group(group);
				clientBootstrap.channel(NioSocketChannel.class);
				clientBootstrap.remoteAddress(new InetSocketAddress("www.google.com", 80));
				clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						socketChannel.pipeline().addLast(new ClientHandler());
					}
				});
				ChannelFuture channelFuture = clientBootstrap.connect().sync();
				channelFuture.channel().closeFuture().sync();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				group.shutdownGracefully().sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
