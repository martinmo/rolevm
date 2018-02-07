package rolevm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Similar to {@link Override}, this annotation is a hint to the type checker
 * that the programmer intents to override a base method. Currently, using this
 * annotation has no effect.
 * 
 * @author Martin Morgenstern
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface OverrideBase {
}
