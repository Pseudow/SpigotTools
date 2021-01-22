package net.pseudow.tools.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandHandler {

    String name();
    String description() default "";
    String usage() default "";
    String author() default "";
    String[] authors() default {};
    String[] permissions() default {};
    String[] aliases() default {};

    int argsLength() default -1;
    boolean requirePlayer() default false;
}
