package se.kodapan.index;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes annotated with this will not contain a primary index.
 *
 * If class previously had a primary index this will be kept.
 * todo remove previous primary index
 *
 * @author kalle
 * @since 2010-aug-24 11:10:01
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoPrimaryIndex {
}
