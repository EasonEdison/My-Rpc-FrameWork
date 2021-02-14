package kiruto.transport.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import kiruto.entity.RpcMessage;
import kiruto.entity.RpcRequest;
import kiruto.entity.RpcResponse;
import kiruto.handler.RequestHandler;
import kiruto.transport.codec.RpcConstants;
import lombok.extern.slf4j.Slf4j;
import naruto.enums.CompressTypeEnum;
import naruto.enums.RpcResponseCodeEnum;
import naruto.enums.SerializationTypeEnum;
import naruto.factory.SingletonFactory;

@Slf4j
public class NettyRpcServerHandler2 extends SimpleChannelInboundHandler<RpcMessage> {
    private final RequestHandler requestHandler;

    public NettyRpcServerHandler2() {
        requestHandler = SingletonFactory.getInstance(RequestHandler.class);
    }

    /**
     * msg 类型是message.
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
        System.out.println("读不读？");
        try {
            if (msg instanceof RpcMessage) {
                log.info("服务器读取信息: {}", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    // 心跳包
                    rpcMessage.setData(RpcConstants.PONG);
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                } else {
                    RpcRequest request = (RpcRequest) ((RpcMessage) msg).getData();
                    Object result = requestHandler.handler(request);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> response = RpcResponse.success(
                            result, request.getRequestId()
                        );
                        rpcMessage.setData(response);
                    } else {
                        RpcResponse<Object> response = RpcResponse.fail(
                            RpcResponseCodeEnum.FAIL, request.getRequestId()
                        );
                        rpcMessage.setData(response);
                        log.error("现在不可写入，丢弃信息");
                    }
                }
                // 这个监听器是如果 !isSuccess 的话，就关闭future
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            log.info("释放信息");
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务器出现异常");
        cause.printStackTrace();
        ctx.close();
    }
}
