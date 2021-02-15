package kiruto.annotation;

import kiruto.spring.ScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用于服务端和客户端的main方法上的，指定要扫描的包的路径.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(ScannerRegistrar.class)
public @interface RpcScan {
    String[] basePackage();
}
