package naruto.exception;

import naruto.enums.RpcErrorMessageEnum;

public class RpcException extends RuntimeException {

    public RpcException(RpcErrorMessageEnum errorMessageEnum, String message) {
        super(errorMessageEnum.getMessage() + " : " + message);
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(RpcErrorMessageEnum errorMessageEnum) {
        super(errorMessageEnum.getMessage());
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }
}
