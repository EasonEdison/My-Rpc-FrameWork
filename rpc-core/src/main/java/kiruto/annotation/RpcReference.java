package kiruto.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用域Client的Controller层的成员变量上，指定Service的group和version.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited  // 可能有子类方法，一并带上
public @interface RpcReference {

    String version() default "";

    String group() default "";
}
