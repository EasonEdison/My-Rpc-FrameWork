package kiruto.registry;

import java.net.InetSocketAddress;

/**
 * 注册服务，将服务名和IP地址绑到一起
 */
public interface ServiceRegistry {
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
