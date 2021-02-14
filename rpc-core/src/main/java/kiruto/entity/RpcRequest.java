package kiruto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcRequest implements Serializable {

    private String requestId;

    private String interfaceName;

    private String methodName;

    private Object[] parameters;

    private Class<?>[] paramTypes;

    private String version;

    private String group;

    public RpcServiceProperties toRpcServiceProperties() {
        return RpcServiceProperties.builder()
            .serviceName(this.interfaceName)
            .group(this.group)
            .version(this.version).build();
    }
}
