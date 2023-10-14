package dev.practice.netty.echo.raw;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ServerResponseHandler extends ChannelOutboundHandlerAdapter { // outbound


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof String) { // 이전 헨들러로 부터 String 타입의 msg 를 받는다.

            log.info("Called ServerResponseHandler::write ... msg: {}", msg);

            String body = (String) msg;
            Charset charset = StandardCharsets.UTF_8;

            ByteBuf buf = ctx.alloc().buffer(); // ByteBuf 생성
            buf.writeCharSequence(body, charset); // Client 에 전달할 응답 데이터 할당


            // ByteBuf 를 다음(prev) OutboundHandler 에 전달한다.
            // 사실 여기 다음은 LoggingHandler -> HeadContext -> Channel 로 ...
            // ByteBuf 를 넘기면 해당 값이 client 로 전달 될 것이다.
            ctx.write(buf, promise);
        }
    }
}
