package kiruto.provider;

import kiruto.entity.RpcServiceProperties;
import lombok.extern.slf4j.Slf4j;
import naruto.enums.RpcErrorMessageEnum;
import naruto.exception.RpcException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;

    public ServiceProviderImpl() {
        // 有必要用这个吗？Bean加载是多线程？
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void addService(Object service, RpcServiceProperties serviceProperties) {
        String rpcServiceName = serviceProperties.toRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, service);
        log.info("注册了新服务: {}", rpcServiceName);
    }

    @Override
    public Object getService(RpcServiceProperties serviceProperties) {
        Object service = serviceMap.get(serviceProperties.toRpcServiceName());
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service, RpcServiceProperties serviceProperties) {
        // 先不做
        return;
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service,
            RpcServiceProperties.builder().group("").version("").build());
    }
}
