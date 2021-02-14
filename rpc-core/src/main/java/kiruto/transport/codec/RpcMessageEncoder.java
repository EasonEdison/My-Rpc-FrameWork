package kiruto.transport.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import kiruto.compress.Compress;
import kiruto.entity.RpcMessage;
import kiruto.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;
import naruto.enums.CompressTypeEnum;
import naruto.enums.SerializationTypeEnum;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 */

@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    // 用原子性数值来生成ID
    private static final AtomicInteger ATOMIC_ID = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf out)
        throws Exception {
        try {
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // 留一部分位置用来存长度，长度是跟body相关的，还要压缩
            out.writerIndex(out.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            out.writeByte(rpcMessage.getCodec());
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            // int，直接占四位; 前面的byte，都是一位
            out.writeInt(ATOMIC_ID.getAndIncrement());
            // 计算全长
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                String codeName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("编码类型为: {}", codeName);
                // 后期可以优化这个
                Serializer serializer = Serializer.getByCode(rpcMessage.getCodec());
                // 先序列化
                bodyBytes = serializer.serializer(rpcMessage.getData());
                Compress compress = Compress.getByCode(rpcMessage.getCompress());
                // 再压缩
                bodyBytes  = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writeIndex = out.writerIndex();
            // 跳回到记录总长的位置，在魔术和版本的后面
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(writeIndex);
            // 再恢复, 妈的，这里是重置index，我注意到了，还是写错了
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("编码 request 出错!", e);
        }
    }
}
