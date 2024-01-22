package dev.practice.netty.echo.raw;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ServerRequestHandler extends ChannelInboundHandlerAdapter { // inbound

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // SocketChannel 의 read 완료 이벤트(READ COMPLETE)가 발생되면 수행되는 두번째의 핸들러이다.
        // 첫번째는 LoggingHandler 로 .. 사실상 read 의 결과가 msg 로 전달 된다.

        // LoggingHandler 를 통해서 SocketChannel 에서 발생되는 모든 이벤트가 로깅되는 걸 봤다... 그런데 여기에 로그를 찍어도 모든 이벤트가 여기까지 도달 하지 않는 것을 확인했다.. 도달 해야할 것 같은데.. 모르겠네..
        //  -> channelRead 는 Netty Channel(여기선 netty SocketChannel) 에서 데이터 읽을 준비되었을 때 호출되는 메서드이다.
        log.info("test");

        if (msg instanceof ByteBuf) { // 이전 핸들러로 부터 전달된 msg 는 ByteBuf 이다.

            try {
                ByteBuf buf = (ByteBuf) msg;

                int len = buf.readableBytes();
                Charset charset = StandardCharsets.UTF_8;
                CharSequence body = buf.readCharSequence(len, charset); // SocketChannel 의 read 결과(ByteBuf) 를 String 으로 변환

                log.info("Called ServerRequestHandler::channelRead ... body: {}", body);

                ctx.fireChannelRead(body); // 다음(next) InboundHandler 에게 값으로 body 를 전달 한다.
            }finally {
                ReferenceCountUtil.release(msg); // ByteBuf 리소스 해제
            }
        }
    }
}
