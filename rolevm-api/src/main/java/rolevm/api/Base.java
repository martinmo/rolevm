package rolevm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be the reference to the base object of the surrounding role.
 * This annotation only has an effect when used inside a class that is annotated
 * with {@link Role}.
 * <p>
 * Using this annotation on a primitive field has undefined behavior.
 * 
 * @author Martin Morgenstern
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Base {
}
