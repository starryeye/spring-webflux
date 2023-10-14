package dev.practice.netty.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyEchoServer {

    @SneakyThrows
    public static void main(String[] args) {

        EventLoopGroup parentGroup = new NioEventLoopGroup(); // ServerSocketChannel 의 accept event 를 처리하기 위함
        EventLoopGroup childGroup = new NioEventLoopGroup(4); // SocketChannel 의 read event 를 처리하기 위함

        EventExecutorGroup eventExecutorGroup = new DefaultEventExecutorGroup(4);


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap
                    .group(parentGroup, childGroup) // EventLoopGroup 넣어준다.
                    .channel(NioServerSocketChannel.class) // parentGroup 은 nioServerSocketChannel 에 대한 accept 이벤트를 받는다고 설정, 내부에서 생성하고 regist 한다.
                    .childHandler(new ChannelInitializer<Channel>() { // accept 이벤트가 완료되었을 때의 수행할 작업을 설정(ChannelInitializer), ChannelInitializer 의 결과는 Channel 이다.
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // accept 이벤트가 다 완료되고 나서 여기로 오면.. read event 를 처리하기 위한 Pipeline 에 새로운 핸들러를 넣어줘야한다.

                            ch.pipeline().addLast(eventExecutorGroup, new LoggingHandler(LogLevel.INFO)); // info 이상 레벨을 로깅해줌
                            ch.pipeline().addLast(
                                    new StringEncoder(), // outbound 상속, String 데이터를 bytebuf 로 변환
                                    new StringDecoder(), // inbound 상속, bytebuf 로 들어오는 데이터를 String 으로 변환
                                    new NettyEchoServerHandler() // 에코 비즈니스 로직
                            );
                        }
                    });

            serverBootstrap
                    .bind(8080)
                    .sync() // bind 될때 까지 대기(blocking)
                    .addListener(new FutureListener<>() {
                        @Override
                        public void operationComplete(Future<Void> future) throws Exception {
                            if(future.isSuccess()) { // bind 성공 시
                                log.info("success to bind 8080");
                            }else {
                                log.error("fail to bind 8080");
                            }
                        }
                    }).channel().closeFuture().sync(); // 채널이 닫힐때 까지 대기하지만(blocking).. 채널이 닫기지 않는다. 즉, 지속적인 실행을 위함

        }finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
            eventExecutorGroup.shutdownGracefully();
        }
    }
}
