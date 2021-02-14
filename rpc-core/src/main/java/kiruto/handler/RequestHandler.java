package kiruto.handler;

import kiruto.entity.RpcRequest;
import kiruto.provider.ServiceProvider;
import kiruto.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;
import naruto.exception.RpcException;
import naruto.factory.SingletonFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 服务端处理客户端的请求报文的处理器
 */
@Slf4j
public class RequestHandler {

    private final ServiceProvider serviceProvider;

    public RequestHandler() {
        this.serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    public Object handler(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.toRpcServiceProperties());
        Object result;
        try {
            // 方法名和参数类型确定唯一方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
