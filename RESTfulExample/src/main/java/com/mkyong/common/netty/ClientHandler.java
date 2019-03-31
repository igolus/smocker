package com.mkyong.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.CharsetUtil;

public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext){
    	
    	String lineSeparator = System.getProperty("line.separator");
    	StringBuilder sb = new StringBuilder();
    	sb.append("GET / HTTP/1.1").append(lineSeparator);
    	sb.append("Host: www.google.com:80").append(lineSeparator);
		//bf.append("Connection: Close").append(lineSeparator);
		//bf.append("Connection: keep-alive").append(lineSeparator);
    	sb.append(lineSeparator);
    	
    	
        try {
			channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(sb.toString(), CharsetUtil.UTF_8));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //channelHandlerContext.close();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause){
        cause.printStackTrace();
        channelHandlerContext.close();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf  in) throws Exception {
		System.out.print("Client received: " + in.toString(CharsetUtil.UTF_8));
		if (in.writerIndex() < in.capacity()) {
			ctx.channel().close();
		}
	}
	
}