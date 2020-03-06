package com.flanderstudy.nettydemo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpUtil.is100ContinueExpected;

/**
 *将读取到的数据进行封装为HttpObject
 */
@Slf4j
public class SimpleServerHandler extends SimpleChannelInboundHandler<FullHttpRequest > {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest  msg) throws Exception {
        log.info(channelHandlerContext.channel().eventLoop().toString());
        log.info(this.toString());
        if (is100ContinueExpected(msg)) {
            channelHandlerContext.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.CONTINUE));
        }
        // 获取请求的uri
        String uri = msg.uri();
        String msgbody = "<html><head><title>test</title></head><body>你请求uri为：" + uri+"</body></html>";
        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(msgbody, CharsetUtil.UTF_8));
        // 设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
        // 将html write到客户端
       channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
