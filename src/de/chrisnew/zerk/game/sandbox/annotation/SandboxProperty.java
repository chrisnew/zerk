/* (c) 2012 Smart Internet Solutions UG (haftungsbeschraenkt) */

package de.chrisnew.zerk.game.sandbox.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SandboxProperty {
	String alias() default "";

	/**
	 * marks exported property as deprecated
	 */
	boolean deprecated() default false;

	/**
	 * features which are required by issuing plugin
	 */
	String[] requiredFeatures() default {};

	/**
	 * this property cannot be overwritten at any time from the sandbox
	 */
	boolean finalized() default false;

	/**
	 * this property cannot be modified
	 */
	boolean readOnly() default false;

	/**
	 * given method name will be called as soon as this property has been modified
	 */
	String writeObserver() default "";

	/**
	 * given method name will be called as soon as this property has been read
	 */
	String readObserver() default "";
}
