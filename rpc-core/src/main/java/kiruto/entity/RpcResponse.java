package kiruto.entity;

import lombok.Data;
import naruto.enums.RpcResponseCodeEnum;

import java.io.Serializable;

@Data
public class RpcResponse<T> implements Serializable {

    private String requestId;

    private Integer code;

    private String message;

    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum responseCodeEnum, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(responseCodeEnum.getCode());
        return response;
    }

}
