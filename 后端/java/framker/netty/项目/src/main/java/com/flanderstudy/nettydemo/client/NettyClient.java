package com.flanderstudy.nettydemo.client;

import com.flanderstudy.nettydemo.handler.ClientHandler;
import com.flanderstudy.nettydemo.handler.ServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

/**
 * @author ldk
 * @version Netty客户端实现
 */
public class NettyClient {

    public static void main(String args[]) throws InterruptedException {

        //1.客户端需要一个无限循环的的事件循环组
        EventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        //2.创建客户端的启动对象
        Bootstrap bootstrap = new Bootstrap();
        //3.设置客户端的配置
        try {
            bootstrap.group(nioEventLoopGroup) //设置线程组
                    .channel(NioSocketChannel.class) //设置客户端通道的实现类
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ClientHandler());// 添加处理器
                        }
                    });
            //客户端进行连接
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6666).sync();
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("这是我写出的数据", CharsetUtil.UTF_8));
            //监听通道关闭信息
            channelFuture.channel().closeFuture().sync();
        }finally {
            //关闭线程组
            nioEventLoopGroup.shutdownGracefully();
        }
    }
}
