/* (c) 2012 Smart Internet Solutions UG (haftungsbeschraenkt) */

package de.chrisnew.zerk.game.sandbox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import de.chrisnew.zerk.game.sandbox.annotation.SandboxClass;
import de.chrisnew.zerk.game.sandbox.annotation.SandboxMethod;
import de.chrisnew.zerk.game.sandbox.annotation.SandboxProperty;

/**
 *
 * @author CR
 *
 * reflects classes which are annotated with SandboxClass
 *
 */
public class SandboxClassReflector {
	private Scriptable reflectedClass = null;
	private String reflectedClassname = "<unknown>";

	private final static HashMap<String, HashMap<?, ?>> methodCaches = new HashMap<String, HashMap<?, ?>> ();
	private HashMap<String, AnonymousFunction> methodCache;

	private boolean isSingleton = false;

	@SuppressWarnings("unchecked")
	public SandboxClassReflector(Scriptable that, boolean useClassCache) {
		if (!that.getClass().isAnnotationPresent(SandboxClass.class)) {
			throw new RuntimeException("no SandboxClass annotation found for " + that.getClass().getName() + "!");
		}

		SandboxClass meta = that.getClass().getAnnotation(SandboxClass.class);

		reflectedClass = that;
		reflectedClassname = meta.className() + "." + meta.packageName() + "/" + meta.version();

		isSingleton = meta.singleton();

		if (useClassCache && methodCaches.containsKey(reflectedClassname)) {
			methodCache = (HashMap<String, AnonymousFunction>) methodCaches.get(reflectedClassname);

			return;
		} else {
			methodCache = new HashMap<String, AnonymousFunction> ();
			prepareMethods();

			if (useClassCache) {
				methodCaches.put(reflectedClassname, methodCache);
			}
		}
	}

	public SandboxClassReflector(Scriptable that) {
		this(that, false);
	}

	private final HashMap<String, SandboxMethod> methodMetaData = new HashMap<String, SandboxMethod>();

	public boolean isMethodFinal(String methodMame) {
		if (isSingleton()) {
			return true;
		}

		if (!methodMetaData.containsKey(methodMame)) {
			return false;
		}

		return methodMetaData.get(methodMame).finalized();
	}

	public boolean isSingleton() {
		return isSingleton;
	}

	private void prepareMethods() {
		Method[] methods = reflectedClass.getClass().getMethods();

		final HashMap<String, Method> overloadedMethodMap = new HashMap<String, Method>();

		for (int i = 0; i < methods.length; i++) {
			final Method method = methods[i];

			if (method.isAnnotationPresent(SandboxMethod.class)) {
				final SandboxMethod sandboxMethodAnnotation = method.getAnnotation(SandboxMethod.class);
				final String methodName = sandboxMethodAnnotation.alias().isEmpty() ? method.getName() : sandboxMethodAnnotation.alias();
				final String overloadedMethodId = methodName + "@" + method.getParameterTypes().length;

				methodMetaData.put(methodName, sandboxMethodAnnotation);
				overloadedMethodMap.put(overloadedMethodId, method);

				if (methodCache.containsKey(methodName)) {
					// overloaded, no need to redefine wrapper function.
					continue;
				}

				methodCache.put(methodName, new AnonymousFunction() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object call(Context arg0, Scriptable arg1, Scriptable arg2, java.lang.Object[] arg3) {
						try {
							Object[] argList = new java.lang.Object[arg3.length];

							String methodId = methodName + "@" + arg3.length;

							// check if we can handle the call appropriately
							if (!overloadedMethodMap.containsKey(methodId)) {
								throw new RuntimeException("wrong argument count");
							}

							// probably overloaded, so take the right method
							Method methodMapped = overloadedMethodMap.get(methodId);
							Class<?>[] paramTypes = methodMapped.getParameterTypes();

							for (int j = 0; j < arg3.length; j++) {
								Class<?> paramType = paramTypes[j];

								argList[j] = Context.jsToJava(arg3[j], paramType);
							}

							return methodMapped.invoke(reflectedClass, argList);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						}

						return null;
					}
				});
			}
		}
	}

	public boolean hasMethod(String name) {
		return methodCache.containsKey(name);
	}

	public AnonymousFunction getMethod(String name) {
		return methodCache.get(name);
	}

	public Object[] getIds() {
		return methodCache.keySet().toArray();
	}

	private final HashMap<String, SandboxProperty> propertyMetaData = new HashMap<String, SandboxProperty>();

	public boolean hasProperty(String name) {
		return propertyMetaData.containsKey(name);
	}

	public Object getProperty(String name) {
		return null;
	}

}
