package com.flanderstudy.nettydemo;

import com.flanderstudy.nettydemo.handler.ServerHandler;
import com.flanderstudy.nettydemo.handler.SimpleServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.ServerBootstrapConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.SystemPropertyUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettydemoApplication {

    //SpringApplication.run(NettydemoApplication.class, args);
    public static void main(String[] args) throws InterruptedException {



        //1.创建bossGroup，用于接收客户端socket请求，连接并绑定到workGroup上
            EventLoopGroup bossGroup = new NioEventLoopGroup(8);
            //2.创建workGroup，用于处理客户端的io请求
            EventLoopGroup workGroup = new NioEventLoopGroup(8);
            //3.创建启动服务
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //4.绑定服务配置
        try {
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)//服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128)//设置线程队列得到的连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)//设置保存活动链接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {//创建一个通道
                        //设置pipeline的处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ServerHandler());// 添加处理器
                            socketChannel.pipeline().addLast(new HttpServerCodec());//添加http处理器
                            socketChannel.pipeline().addLast("httpAggregator",new HttpObjectAggregator(512*1024));
                            socketChannel.pipeline().addLast(new SimpleServerHandler());
                        }
                    });//workGroup的对应管道设置处理器
            //5.服务器启动,绑定端口并且同步返回ChannelFuture
            ChannelFuture channelFuture = serverBootstrap.bind(8888).sync();
            ChannelFuture closeFuture = channelFuture.channel().closeFuture().sync();
            closeFuture.addListener(new GenericFutureListener(){

                @Override
                public void operationComplete(Future future) throws Exception {
                    if(future.isSuccess()){
                        System.out.println("close");
                    }
                }
            });

        } finally {
            //关闭bossGroup线程组
            bossGroup.shutdownGracefully();
            //关闭workGroup线程组
            workGroup.shutdownGracefully();
        }
    }
}
