package se.kodapan.entitystore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kalle
 * @since 2011-10-16 01.45
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Index {

  /** todo: not implemented */
  public abstract String multiplicity() default "0..*";

  /** todo: can be shared between several fields in order to create a composite key class */
  public abstract String name();
}
