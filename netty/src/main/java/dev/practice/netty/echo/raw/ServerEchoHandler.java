package dev.practice.netty.echo.raw;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerEchoHandler extends ChannelInboundHandlerAdapter { // inbound

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof String) { // 이전 handler 로 부터 String 타입의 msg 수신됨

            String request = (String) msg;

            log.info("Called ServerEchoHandler::channelRead ... msg: {}", request);

            // writeAndFlush 로 다음(prev) OutboundHandler 에게 전달한다. (지금까지 inbound 방향이다가.. 방향이 바뀐 것이다.)
            ctx.writeAndFlush(request) // 에코 서버이므로 요청 데이터를 그대로 응답 데이터로 내려준다.
                    .addListener(ChannelFutureListener.CLOSE); // SocketChannel(채널) 에 write 이 완료 되면(응답 데이터 전송 완료) SocketChannel(채널)을 닫는다.
        }
    }
}
