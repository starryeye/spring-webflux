package dev.practice.netty.http;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;

public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof FullHttpRequest) { //NettyHttpServerHandler 의 앞에서 HttpObjectAggregator 핸들러의 작업결과로 FullHttpRequest 가 전달된다.

            FullHttpRequest request = (FullHttpRequest) msg; // 요청 데이터

            // 반대로 응답으로는 FullHttpResponse 를 보내면 알아서 적절하게 변환된다. (역순)
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);

            response.headers().set("Content-Type", "text/plain");
            response.content().writeCharSequence("Hello, world!", StandardCharsets.UTF_8);

            ctx.writeAndFlush(response) // 응답 데이터 전송
                    .addListener(ChannelFutureListener.CLOSE); // close
        }
    }
}
