package dev.practice.netty.echo.raw;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyEchoRawServer {
    /**
     * ServerBootstrap 을 사용하지 않는다. (편의 기능X)
     * 좀더 raw 한 코드이며 
     * 따라서 Netty 동작에 대해 더 자세히 알아볼 수 있다.
     *
     * ServerBootstrap 을 사용한 버전(NettyEchoServer.java) 에서는...
     * - NioServerSocketChannel "class" 클래스 인스턴스를 Bootstrap 에 넘기면 NioServerSocketChannel 생성을 알아서 해줬다.
     * - NioServerSocketChannel 의 accept 이벤트 핸들러를 구현하지 않아도 된다.
     * - Encoder, Decoder 를 사용하여 Server(Request/Response)Handler 를 구현하지 않아도 되었다.
     *
     * client 는 NettyEchoClient 를 사용하면 됨.
     */

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);

        NioServerSocketChannel serverSocketChannel = new NioServerSocketChannel(); // NioServerSocketChannel 을 직접 생성한다.
        bossGroup.register(serverSocketChannel); // 생성한 채널을 EventLoopGroup 에 등록한다. -> bossGroup 은 accept 이벤트를 처리하게 됨

        serverSocketChannel.pipeline().addLast(
                // serverSocketChannel(채널)이 갖는 ChannelPipeline 에 ServerAcceptHandler (채널 핸들러) 를 등록한다. 즉, accept 이벤트가 발생하면 acceptor 가 수행된다.
                new ServerAcceptHandler(workerGroup) // accept 결과로 SocketChannel 이 결과로 받아지고 해당 채널의 read 완료 이벤트를 workerGroup (EventLoopGroup) 이 담당하도록 할 것이므로 파라미터로 같이 넘겨줌
        );

        // NioServerSocketChannel 을 바인드 시킴, 바인드에 성공하면 특정 로그를 찍는다.
        serverSocketChannel.bind(new InetSocketAddress(8080))
                .addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("server bound to port 8080");
                    }
                });

    }
}
