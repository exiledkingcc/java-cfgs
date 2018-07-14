package org.exiledkingcc.java.cfgs.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigKey {
    String key();
    boolean optional() default false;
    Class itemType() default Void.class;
}
