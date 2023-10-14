package dev.practice.netty;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyEchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof String) { // NettyEchoServerHandler 앞에서는 StringDecoder 로 ByteBuf 를 String 으로 변환해준다.

            log.info("Receive: {}", msg);

            ctx.writeAndFlush(msg) // 에코 서버니까 값을 그대로 내보낸다.(outbound)
                    .addListener(ChannelFutureListener.CLOSE); // future.channel().close()
        }
    }
}
