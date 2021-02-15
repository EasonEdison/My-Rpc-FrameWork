package kiruto.loadbalance;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {

    /**
     * 先做一个初步的检查.
     */
    @Override
    public String selectServiceAddress(List<String> serviceAddress, String serviceName) {
        if (serviceAddress == null || serviceAddress.size() == 0) {
            return null;
        }
        if (serviceAddress.size() == 1) {
            return serviceAddress.get(0);
        }
        return doSelect(serviceAddress, serviceName);
    }

    protected abstract String doSelect(List<String> serviceAddress, String serviceName);
}
