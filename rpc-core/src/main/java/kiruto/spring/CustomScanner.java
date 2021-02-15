package kiruto.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

public class CustomScanner extends ClassPathBeanDefinitionScanner {
    // 自定义构造器
    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        super(registry);
        // 增加扫描过滤时要包含的类型，为了计数，去掉component
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
        // super.addExcludeFilter(new AnnotationTypeFilter(Component.class));
    }

    /**
     * 指定要扫描的包.
     */
    @Override
    public int scan(String... basePackages) {
        return super.scan(basePackages);
    }
}
