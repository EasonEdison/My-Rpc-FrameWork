package kiruto.transport.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import kiruto.compress.Compress;
import kiruto.entity.RpcMessage;
import kiruto.entity.RpcRequest;
import kiruto.entity.RpcResponse;
import kiruto.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH,
            5,  // 记录长度的位置的起点
            4,  // 记录长度的内容的大小
            -9,
            0);
    }

    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        // 如果是ByteBuf，说明没问题，可以做处理
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("解码帧发生错误!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    /**
     * 按着顺序来一点点解析.
     */
    private Object decodeFrame(ByteBuf in) {
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();

        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
            .codec(codecType)
            .messageType(messageType)
            .requestId(requestId).build();
        // 心跳
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            // 信息放到bs中
            in.readBytes(bs);
            // 先解压，再反序列化
            Compress compress = Compress.getByCode(compressType);
            bs = compress.decompress(bs);
            Serializer serializer = Serializer.getByCode(codecType);
            // log.info("编码类型为: {}", SerializationTypeEnum.getName(codecType));
            // 请求、响应是不用类型
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest rpcRequest = serializer.deserializer(bs, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            }
            if (messageType == RpcConstants.RESPONSE_TYPE) {
                RpcResponse rpcResponse = serializer.deserializer(bs, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            }
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf in) {
        // 版本只有一个字节
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("版本不匹配!");
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] magicNumber = new byte[len];
        in.readBytes(magicNumber);
        for (int i = 0; i < len; i++) {
            // 逐字节比较
            if (magicNumber[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("无法识别的magic: " + Arrays.toString(magicNumber));
            }
        }
    }
}
