package kiruto.entity;


import lombok.Builder;
import lombok.Data;

/**
 * 服务注册相关信息类.
 */
@Data
@Builder
public class RpcServiceProperties {

    private String serviceName;

    private String group;

    private String version;

    public String toRpcServiceName() {
        return this.serviceName + this.group + this.version;
    }
}
