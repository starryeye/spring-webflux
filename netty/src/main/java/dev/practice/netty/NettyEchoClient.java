package dev.practice.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyEchoClient {

    @SneakyThrows
    public static void main(String[] args) {

        EventLoopGroup workerGroup = new NioEventLoopGroup(1); // SocketChannel 의 connect 이벤트를 처리하기 위한 EventLoopGroup

        try {

            Bootstrap bootstrap = new Bootstrap(); // 일반 Bootstrap

            bootstrap // EventLoopGroup 설정, 채널 설정, 핸들러 설정
                    .group(workerGroup)
                    .channel(NioSocketChannel.class) //workerGroup 은 NioSocketChannel(채널) 의 connect 이벤트를 받는다고 설정, 요청을 보낼때 내부에서 NioSocketChannel 을 생성하고 regist
                    .handler(new ChannelInitializer<Channel>() { // connect 된 이후에 처리될 작업을 ChannelPipeline 으로서 등록한다. 여기서의 Channel 은 SocketChannel 이다.
                        @Override
                        protected void initChannel(Channel ch) throws Exception {

                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new StringEncoder(),
                                    new StringDecoder(),
                                    new NettyEchoClientHandler()
                            );
                        }
                    });

            bootstrap
                    .connect("localhost", 8080) // connect 수행
                    .sync() // connect 가 되기 전까지 blocking
                    .channel().closeFuture()
                    .sync(); // close 가 되기 전까지 blocking

            //1회 요청 이후 종료


        }finally {
            workerGroup.shutdownGracefully();
        }
    }
}
