package com.daemitus.deadbolt.config;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigComment {

    String[] value() default "";
}
