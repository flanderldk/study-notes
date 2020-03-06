package com.flanderstudy.nettydemo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * @author ldk
 */
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 通道读取到的数据，
     * @param ctx  ChannelHandlerContext是通道的上下文，可以获得通道，处理器等数据
     * @param msg  msg是客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //ByteBuf是netty封装的数据缓冲，本质上是字节数组
        ByteBuf msgByteBuf = (ByteBuf) msg;
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        log.info("客户端" + socketAddress.toString() + "发送的数据是： " + msgByteBuf.toString(CharsetUtil.UTF_8));
        ctx.channel().eventLoop().execute(() ->{
            try {
                Thread.sleep(4000);
                System.out.println(Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("正在执行任务1");
        });
        ctx.channel().eventLoop().execute(() ->{
            try {
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName());  //线程相同
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("正在执行任务2");
        });
        //super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("读取客户端数据完毕");
        log.info("正在回复");
        //服务器回复信息时，需要进行编码  "已经成功接受到消息".getBytes(CharsetUtil.UTF_8)
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("已经成功接受到消息", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("发生异常");
        ctx.channel().close();
    }
}
