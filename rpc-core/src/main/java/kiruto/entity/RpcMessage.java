package kiruto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报文相关信息类.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcMessage {

    private byte messageType;

    private byte codec;

    private byte compress;

    // 类型不同？
    private int requestId;

    private Object data;
}
