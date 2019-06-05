package cz.neumimto.rpg.api.skills.scripting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsBinding {

    Type value();

    enum Type {
        CLASS,
        OBJECT,
        CONTAINER
    }
}
