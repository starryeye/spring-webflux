package dev.practice.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyHttpServer {

    @SneakyThrows
    public static void main(String[] args) {

        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup(4);

        EventExecutorGroup eventExecutorGroup = new DefaultEventExecutorGroup(4);


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap
                    .group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {

                            ch.pipeline().addLast(eventExecutorGroup, new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast(

                                    new HttpServerCodec(), //HTTP 를 분석해서 Http 객체를 생성(LastHttpContent, HttpMessages, HttpContents)하여 next
                                    new HttpObjectAggregator(1024 * 1024), // 1MB 사이즈로 limit, 위에서 생성된 객체를 사용하기 쉽도록 FullHttpRequest, FullHttpResponse 객체로 변환 후 next
                                    new NettyHttpServerHandler() // 비즈니스 로직
                            );
                        }
                    });

            serverBootstrap
                    .bind(8080)
                    .sync()
                    .addListener(new FutureListener<>() {
                        @Override
                        public void operationComplete(Future<Void> future) throws Exception {
                            if(future.isSuccess()) {
                                log.info("success to bind 8080");
                            }else {
                                log.error("fail to bind 8080");
                            }
                        }
                    }).channel().closeFuture().sync();

        }finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
            eventExecutorGroup.shutdownGracefully();
        }
    }
}
