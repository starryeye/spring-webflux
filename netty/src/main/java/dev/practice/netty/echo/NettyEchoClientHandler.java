package dev.practice.netty.echo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyEchoClientHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // client 가 server 와의 연결이 준비 되었을 때의 로직을 담당

        ctx.writeAndFlush("I am Client."); // 서버로 데이터 전송

        // 여기서 ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
        // 를 하면.. 서버가 응답을 보내기전에 닫는 것이므로 문제가 될 수 있음..
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 서버에서 데이터를 보내서.. read 이벤트가 발생 했을 때의 로직을 담당

        if (msg instanceof String) {
            log.info("Received: {}", msg);
        }

        // ctx.fireChannelRead() 서버로 응답을 받아 로깅하고 더이상의 작업은 없으므로 fireChannelRead 를 할 필요는 없다.
    }
}
