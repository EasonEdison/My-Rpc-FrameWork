package kiruto.transport.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import kiruto.entity.RpcMessage;
import kiruto.entity.RpcResponse;
import kiruto.transport.codec.RpcConstants;
import lombok.extern.slf4j.Slf4j;
import naruto.enums.CompressTypeEnum;
import naruto.enums.SerializationTypeEnum;
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
            // log.info("客户端收到消息: {}", msg);
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


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 如果出发了写空闲，就发个Ping给服务端
            if (state == IdleState.WRITER_IDLE) {
                Channel channel = ctx.channel();
                log.info("发送写空闲包: {}", channel.remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

}
