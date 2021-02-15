package kiruto.spring;

import kiruto.annotation.RpcScan;
import kiruto.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;

/**
 *获取有指定注解的的类，加载到容器中.
 */
@Slf4j
public class ScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    // 好像直接把这个覆盖了
    private static final String SPRING_BEAN_BASE_PACKAGE = "kiruto.spring";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    /**
     * 获取资源加载器.
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 获取注解中的值.
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry registry) {
        // 获取指定注解的元属性
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes
            .fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        // 读取注解中指定字段的值
        String[] rpcScanBasePackage = new String[0];
        if (rpcScanAnnotationAttributes != null) {
            rpcScanBasePackage = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        // 如果啥也没读到，就...扫描一个最基本的包？
        if (rpcScanBasePackage.length == 0) {
            rpcScanBasePackage = new String[]{((StandardAnnotationMetadata) annotationMetadata
                                              ).getIntrospectedClass().getPackage().getName()};
        }
        // 自定义注解扫描器
        CustomScanner serviceScanner = new CustomScanner(registry, RpcService.class);
        // CustomScanner componentScanner = new CustomScanner(registry, Component.class);
        if (resourceLoader != null) {
            serviceScanner.setResourceLoader(resourceLoader);
            // componentScanner.setResourceLoader(resourceLoader);
        }
        int serviceNum = serviceScanner.scan(rpcScanBasePackage);
        // int componentNum = componentScanner.scan(rpcScanBasePackage);
        // componentScanner.scan(rpcScanBasePackage);
        // spring、NettyServer
        log.info("扫描到的服务数量: {}", serviceNum - 2);
    }
}
