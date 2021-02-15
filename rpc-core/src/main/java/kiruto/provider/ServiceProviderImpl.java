package kiruto.provider;

import kiruto.entity.RpcServiceProperties;
import kiruto.registry.ServiceRegistry;
import kiruto.registry.zk.ZKServiceRegistry;
import kiruto.transport.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;
import naruto.enums.RpcErrorMessageEnum;
import naruto.exception.RpcException;
import naruto.factory.SingletonFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ServiceProviderImpl() {
        // 有必要用这个吗？Bean加载是多线程？
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.serviceRegistry = SingletonFactory.getInstance(ZKServiceRegistry.class);
    }

    @Override
    public void addService(Object service, RpcServiceProperties serviceProperties) {
        String serviceName = service.getClass().getInterfaces()[0].getName();
        serviceProperties.setServiceName(serviceName);
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
        try {
            // 获取本机地址
            String host = InetAddress.getLocalHost().getHostAddress();
            // 获取服务的接口类，从而得到接口名, 第一个就是想要的
            Class<?> interfaces = service.getClass().getInterfaces()[0];
            String serviceName = interfaces.getCanonicalName();
            serviceProperties.setServiceName(serviceName);
            // 添加到内存中
            this.addService(service, serviceProperties);
            // 注册到zk中, 注意name是服务、版本、组
            serviceRegistry.registerService(serviceProperties.toRpcServiceName(),new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service,
            RpcServiceProperties.builder().group("").version("").build());
    }
}
