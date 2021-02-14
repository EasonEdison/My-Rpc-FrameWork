package kiruto.proxy;

import kiruto.entity.RpcRequest;
import kiruto.entity.RpcResponse;
import kiruto.entity.RpcServiceProperties;
import kiruto.transport.RpcClient;
import lombok.extern.slf4j.Slf4j;
import naruto.enums.RpcErrorMessageEnum;
import naruto.exception.RpcException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final RpcClient rpcClient;
    // 用来获取group、version信息的
    private final RpcServiceProperties properties;

    public RpcClientProxy(RpcClient rpcClient, RpcServiceProperties properties) {
        this.rpcClient = rpcClient;
        this.properties = properties;
    }

    // 最后的this就是设置InvocationHandler，调用方法的时候会执行invoke
    public <T> T getProxyInstance(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
            new Class<?>[]{clazz}, this);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("调用方法: {}", method.getName());
        // 生成request
        RpcRequest rpcRequest = RpcRequest.builder()
            .interfaceName(method.getDeclaringClass().getName())
            .methodName(method.getName())
            .paramTypes(method.getParameterTypes())
            .parameters(args)
            .requestId(UUID.randomUUID().toString())    // 随机生成一个ID
            .group(this.properties.getGroup())
            .version(this.properties.getVersion())
            .build();
        // 通过future获取返回的信息
        RpcResponse<Object> rpcResponse = null;
        CompletableFuture<RpcResponse<Object>> future = (CompletableFuture<RpcResponse<Object>>) rpcClient
            .sendRequest(rpcRequest);
        System.out.println("send也没问题");
        rpcResponse = future.get();
        check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    /**
     * 检查返回的结果的正确性，如UUID、编码类型、是否非空
     * @param rpcResponse
     * @param rpcRequest
     */
    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, rpcRequest.getInterfaceName());
        }
        if (rpcRequest.getRequestId() != rpcRequest.getRequestId()) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE.getMessage());
        }
    }
}
