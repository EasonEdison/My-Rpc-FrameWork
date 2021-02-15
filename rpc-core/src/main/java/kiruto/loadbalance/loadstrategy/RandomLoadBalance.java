package kiruto.loadbalance.loadstrategy;

import kiruto.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 随机挑一个
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> serviceAddress, String serviceName) {
        Random random = new Random();
        return serviceAddress.get(random.nextInt(serviceAddress.size()));
    }
}
