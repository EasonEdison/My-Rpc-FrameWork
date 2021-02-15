package kiruto.spring;

import kiruto.annotation.RpcReference;
import kiruto.annotation.RpcService;
import kiruto.entity.RpcServiceProperties;
import kiruto.provider.ServiceProvider;
import kiruto.provider.ServiceProviderImpl;
import kiruto.proxy.RpcClientProxy;
import kiruto.transport.RpcClient;
import kiruto.transport.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import naruto.factory.SingletonFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

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
            serviceProvider.publishService(bean, rpcServiceProperties);
        }
        return bean;
    }

    /**
     * 自动对标有RpcReference的成员变量进行动态代理方法的生成.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        // 返回接口或类中声明的成员变量
        Field[] declaredFields = targetClass.getDeclaredFields();
        // 找到带RpcReference注解的成员变量
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                // 获取注解的值，即服务信息
                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                    .group(rpcReference.group()).version(rpcReference.version()).build();
                // 根据信息生成代理类
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);
                Object proxyInstance = rpcClientProxy.getProxyInstance(declaredField.getType());
                // 改变成员变量的访问权限，讲成员变量替换成刚生成的代理类
                declaredField.setAccessible(true);
                try {
                    // 第一个参数是对象变量，即该成员变量在那个对象中进行替换
                    // 第二个参数就是要替换的新的值
                    declaredField.set(bean, proxyInstance);
                } catch (IllegalAccessException e) {
                    log.error("替换方法失败: []", declaredField.getName());
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
