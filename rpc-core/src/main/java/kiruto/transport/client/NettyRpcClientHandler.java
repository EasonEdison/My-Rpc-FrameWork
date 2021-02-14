package kiruto.transport.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import kiruto.entity.RpcMessage;
import kiruto.entity.RpcResponse;
import kiruto.transport.codec.RpcConstants;
import lombok.extern.slf4j.Slf4j;
import naruto.factory.SingletonFactory;

@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequest unprocessedRequest;
    private final NettyRpcClient rpcClient;

    public NettyRpcClientHandler() {
        unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
        rpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 读取从服务端返回的响应报文, 并且将结果放到future中.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 先来个简单的
        try {
            log.info("客户端收到消息: {}", msg);
            if (msg instanceof RpcMessage) {
                RpcMessage rpcMessage = (RpcMessage) msg;
                byte messageType = rpcMessage.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("心跳: {}", rpcMessage.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> responseData = (RpcResponse<Object>) rpcMessage.getData();
                    unprocessedRequest.complete(responseData);
                }
            }
        } finally {
            // 释放资源
            ReferenceCountUtil.release(msg);
        }
    }

    // 先不做心跳处理

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

}
