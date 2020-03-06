package com.flanderstudy.nettydemo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //super.channelActive(ctx);
        log.info("客户端已经连接:" + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("客户端发送消息", CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        ByteBuf msgByteBuf = (ByteBuf) msg;
        log.info("客户端读取到数据:" + msgByteBuf.toString(CharsetUtil.UTF_8));
    }
}
