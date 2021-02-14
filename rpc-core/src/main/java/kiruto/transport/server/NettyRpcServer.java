package kiruto.transport.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import kiruto.entity.RpcServiceProperties;
import kiruto.provider.ServiceProviderImpl;
import kiruto.transport.RpcServer;
import kiruto.transport.codec.RpcMessageDecoder;
import kiruto.transport.codec.RpcMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import naruto.factory.SingletonFactory;

@Slf4j
public class NettyRpcServer implements RpcServer {

    // private String host = "192.168.153.128";
    private String host = "localhost";

    private Integer port = 9001;

    private final ServiceProviderImpl ServiceProviderImpl;

    public NettyRpcServer() {
        ServiceProviderImpl = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    public void registerService(Object service, RpcServiceProperties rpcServiceProperties) {
        String serviceName = service.getClass().getInterfaces()[0].getName();
        rpcServiceProperties.setServiceName(serviceName);
        // rpcServiceProperties.setServiceName(service.getClass().getDeclaringClass().getName());
        ServiceProviderImpl.addService(service, rpcServiceProperties);
    }

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // Nagle算法？好像是防止糊涂窗口还是啥的？
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 用于临时存放三次握手请求的队列的最大长度（就是那个receive吧）
                .option(ChannelOption.SO_BACKLOG, 128)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 先不加心跳试试
                        // pipeline.addLast(new IdleStateHandler(
                        //     30,0,0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcServerHandler());
                    }
                });
            // 同步方法等待绑定成功
            ChannelFuture future = bootstrap.bind(this.host, this.port).sync();
            log.info("服务端绑定成功");
            // 同步方法监听关闭，只有关闭了才会继续往下运行
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("服务端Netty连接出错: ", e);
        } finally {
            log.info("服务端已关闭...");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
