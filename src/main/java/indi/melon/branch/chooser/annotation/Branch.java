package indi.melon.branch.chooser.annotation;

import java.lang.annotation.*;

/**
 * @author vvnn1
 * @since 2024/7/19 20:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Branch {
    String mainName();
}
