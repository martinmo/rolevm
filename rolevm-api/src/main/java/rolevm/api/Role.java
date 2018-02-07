package rolevm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class to be a role type. A role type must be a public inner class of
 * a type that extends {@link Compartment}.
 * 
 * @author Martin Morgenstern
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Role {
}
