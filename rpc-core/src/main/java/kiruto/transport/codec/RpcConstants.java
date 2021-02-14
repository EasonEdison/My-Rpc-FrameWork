package kiruto.transport.codec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RpcConstants {

    public static final byte[] MAGIC_NUMBER = {
        (byte) 'M', (byte) 'R', (byte) 'P', (byte) 'C'};

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static final byte VERSION = 1;
    // 这个总长是什么？首部长度吗？
    public static final byte TOTAL_LENGTH = 16;

    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;

    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    public static final byte HEAD_LENGTH = 16;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    // 为什么是这么大？最大8M一帧？
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
