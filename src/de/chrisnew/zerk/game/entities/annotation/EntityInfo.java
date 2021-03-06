package de.chrisnew.zerk.game.entities.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EntityInfo {
	String name() default "";
	String description() default "";

	/**
	 * by true, this entity class won't be shown in editor, but it's still creatable
	 */
	boolean virtual() default false;
}
