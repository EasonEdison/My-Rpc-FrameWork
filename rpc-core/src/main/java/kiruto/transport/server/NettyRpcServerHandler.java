package kiruto.transport.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RequestHandler requestHandler;

    private  String codeName;

    public NettyRpcServerHandler() {
        try {
            InputStream in = NettyRpcServerHandler.class.getResourceAsStream("/serializer.properties");
            Properties properties = new Properties();
            properties.load(in);
            codeName = properties.getProperty("codename");
            log.info("编码类型为: {}", codeName);
        } catch (IOException e) {
            codeName = "kyro";
            log.info("编码类型未设置，使用默认值: kyro");
        }
        requestHandler = SingletonFactory.getInstance(RequestHandler.class);
        // 读取配置文件序列化内容
    }

    /**
     * msg 类型是message.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                log.info("服务器读取信息: {}", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.getCode(codeName));
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    // 心跳包
                    rpcMessage.setData(RpcConstants.PONG);
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                } else {
                    // 这里并没有设置rpcMessage的Id，因为检查的时候直接用的response的Id
                    RpcRequest request = (RpcRequest) ((RpcMessage) msg).getData();
                    Object result = requestHandler.handler(request);
                    // 收到的request，返回就是response
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> response = RpcResponse.success(
                            result, request.getRequestId()
                        );
                        log.info("信息为: {}", response);
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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("写空闲触发，关闭通道: {}", ctx.channel());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务器出现异常");
        cause.printStackTrace();
        ctx.close();
    }
}
