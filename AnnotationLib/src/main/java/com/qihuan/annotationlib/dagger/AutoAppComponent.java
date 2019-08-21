package com.qihuan.annotationlib.dagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AutoAppComponent
 *
 * @author qi
 * @date 2019-08-21
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface AutoAppComponent {
    Class<?>[] modules() default {};
}
