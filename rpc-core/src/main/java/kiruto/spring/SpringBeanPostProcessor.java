package kiruto.spring;

import kiruto.annotation.RpcService;
import kiruto.entity.RpcServiceProperties;
import kiruto.provider.ServiceProvider;
import kiruto.provider.ServiceProviderImpl;
import kiruto.transport.RpcClient;
import kiruto.transport.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import naruto.factory.SingletonFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 这个要通过spring加载，作用是自动扫描注解并注册.
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcClient rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
        this.rpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 初始化之前执行，选出带有RpcService注解的bean，执行注册.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 如果这个bean带有该注解
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[] 带有注解: []", beanName, RpcService.class.getCanonicalName());
            // 获取注解带有的信息，进行注册
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group(annotation.group())
                .version(annotation.version()).build();
            serviceProvider.addService(bean, rpcServiceProperties);
        }
        return bean;
    }

    /**
     * 自动对标有RpcReference的成员变量进行动态代理方法的生成.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
