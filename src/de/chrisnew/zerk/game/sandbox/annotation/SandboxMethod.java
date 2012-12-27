/* (c) 2012 Smart Internet Solutions UG (haftungsbeschraenkt) */

package de.chrisnew.zerk.game.sandbox.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SandboxMethod {
	String alias() default "";

	/**
	 * marks exported method as deprecated
	 */
	boolean deprecated() default false;

	/**
	 * this method cannot be overwritten at any time from the sandbox
	 */
	boolean finalized() default false;
}
