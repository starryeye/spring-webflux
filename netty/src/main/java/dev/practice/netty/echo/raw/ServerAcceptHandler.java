package dev.practice.netty.echo.raw;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerAcceptHandler extends ChannelInboundHandlerAdapter { // inbound

    DefaultEventExecutorGroup executorGroup = new DefaultEventExecutorGroup(4);

    private final EventLoopGroup childGroup;

    public ServerAcceptHandler(EventLoopGroup childGroup) {
        this.childGroup = childGroup;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // NioServerSocketChannel (채널) 에서 accept 이벤트가 발생되면 수행되는 메서드이다.
        // ServerSocketChannel 의 Read 이벤트는 accept 이다. 그래서 메서드 명이 channelRead 인 것이다.

        log.info("Called ServerAcceptHandler::channelRead ...");

        if (msg instanceof SocketChannel) { // NioServerSocketChannel 의 accept 결과(SocketChannel)가 msg 로 전달 된다.

            SocketChannel socketChannel = (SocketChannel) msg; // netty 라이브러리의 SocketChannel 이다. Java nio 와 전혀 다름

            socketChannel.pipeline().addLast(
                    // socketChannel (채널) 이 갖는 ChannelPipeline 에 채널 핸들러로 LoggingHandler 를 등록한다. 즉, socketChannel 의 read 완료(READ COMPLETE) 이벤트가 발생하면 수행된다.
                    // 파라미터로 EventExecutor 을 전달했다. -> LoggingHandler 는 EventLoop 로 수행하지 않고 별도의 스레드(EventExecutor)로 수행한다.
                    executorGroup, new LoggingHandler(LogLevel.INFO)
            );

            socketChannel.pipeline().addLast(
                    new ServerRequestHandler(), // LoggingHandler 다음의 핸들러로 등록
                    new ServerResponseHandler(), // ServerRequestHandler 다음의 핸들러로 등록
                    new ServerEchoHandler() // ServerResponseHandler 다음의 핸들러로 등록
            );

            // 참고
            // SocketChannel 에서 read 가 준비 되면 수행될 pipe line
            // 1.HeadContext -> 2.LoggingHandler -> 3.ServerRequestHandler ->  4.ServerEchoHandler
            // ServerEchoHandler 에서 writeAndFlush 로 방향 전환됨 (inbound -> outbound)
            // 4.ServerEchoHandler -> 5.ServerResponseHandler -> 6.LoggingHandler -> 7.HeadContext


            // SocketChannel (채널)을 EventLoopGroup 에 등록한다. -> childGroup 은 SocketChannel 의 read 이벤트를 처리하게 됨.
            childGroup.register(socketChannel);
        }
    }
}
