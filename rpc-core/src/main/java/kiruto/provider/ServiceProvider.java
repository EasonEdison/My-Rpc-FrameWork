package kiruto.provider;

import kiruto.entity.RpcServiceProperties;

/**
 * 提供服务的工具类.
 * service：各个服务的实现类
 * publish：将服务信息和本机地址注册到zookeeper
 */
public interface ServiceProvider {

    void addService(Object service, RpcServiceProperties serviceProperties);

    Object getService(RpcServiceProperties serviceProperties);

    void publishService(Object service, RpcServiceProperties serviceProperties);

    void publishService(Object service);
}
