package kiruto.loadbalance;

import java.util.List;

/**
 * 负载均衡，选一个地址
 */
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceAddress, String serviceName);
}
