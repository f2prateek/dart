package com.f2prateek.dart;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Retention(CLASS)
@Target(TYPE)
public @interface HensonNavigable {
    Class<?> model() default Void.class;
}
