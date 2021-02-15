package kiruto.transport.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import kiruto.entity.RpcMessage;
import kiruto.entity.RpcRequest;
import kiruto.entity.RpcResponse;
import kiruto.registry.ServiceDiscovery;
import kiruto.registry.zk.ZKServiceDiscovery;
import kiruto.transport.RpcClient;
import kiruto.transport.codec.RpcConstants;
import kiruto.transport.codec.RpcMessageDecoder;
import kiruto.transport.codec.RpcMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import naruto.enums.CompressTypeEnum;
import naruto.enums.SerializationTypeEnum;
import naruto.factory.SingletonFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 这个类是给proxy用的
 */
@Slf4j
public class NettyRpcClient implements RpcClient {

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private final UnprocessedRequest unprocessedRequest;
    private final ClientChannelProvider channelProvider;
    private final ServiceDiscovery serviceDiscovery;
    private String host = "localhost";
    private int port = 9001;

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            // 5s的连接超时时间
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    // 心跳, 先不用
                    // pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                    pipeline.addLast(new RpcMessageEncoder());
                    pipeline.addLast(new RpcMessageDecoder());
                    // 添加自定义的处理器
                    pipeline.addLast(new NettyRpcClientHandler());
                }
            });

        this.unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
        this.channelProvider = SingletonFactory.getInstance(ClientChannelProvider.class);
        this.serviceDiscovery = SingletonFactory.getInstance(ZKServiceDiscovery.class);
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.toRpcServiceProperties().toRpcServiceName();
        // 查找服务
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(serviceName);
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        Channel channel = getChannel(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            // 添加到任务处理列表, 方便Handler处理
            unprocessedRequest.put(rpcRequest.getRequestId(), resultFuture);
            // 包装成rpcMessage，写入到channel里
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setData(rpcRequest);
            rpcMessage.setMessageType(RpcConstants.REQUEST_TYPE);
            rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
            // 但是根本收不到消息，说明还是没法送出去？还是说那边接受有问题？
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println();
                    log.info("客户端信息发送成功: {}", rpcMessage);
                } else {
                    log.error("客户端信息发送失败!");
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                }
            });

        } else {
            log.error("通道获取异常");
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    private Channel getChannel(InetSocketAddress inetSocketAddress) {
        // 先尝试读缓存
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            if (channel != null) {
                channelProvider.set(inetSocketAddress, channel);
            }
        }
        return channel;
    }

    /**
     * 建立连接获取channel.
     */
    private Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // 指定特定的监听类型，我觉得这块可以用sync
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端连接 {} 成功！", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException("客户端连接失败！");
            }
        });
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.info("获取channel失败");
            e.printStackTrace();
            return null;
        }
    }
}
