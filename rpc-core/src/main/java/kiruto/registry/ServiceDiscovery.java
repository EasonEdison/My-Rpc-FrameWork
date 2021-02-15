package kiruto.registry;

import java.net.InetSocketAddress;

/**
 * 查找服务，返回IP地址.
 */
public interface ServiceDiscovery {
    InetSocketAddress lookupService(String rpcServiceName);
}
