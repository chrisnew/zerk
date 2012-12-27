/* (c) 2012 Smart Internet Solutions UG (haftungsbeschraenkt) */

package de.chrisnew.zerk.game.sandbox;

import java.util.HashMap;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import de.chrisnew.zerk.game.sandbox.annotation.SandboxClass;
import de.chrisnew.zerk.game.sandbox.annotation.SandboxMethod;

/**
 *
 * @author CR
 *
 * @param <T> use Object for T if type is not known to compile time.
 */
public abstract class AbstractSandboxWrapper<T> implements UnknownSandboxWrapper, Scriptable {
	protected T object;
	protected final Scriptable scope;

	private Scriptable parentScope = null, prototype = null;

	private final String className;

	private final SandboxClassReflector reflector;

	private final HashMap<String, Object> overwrittenProperties = new HashMap<String, Object> ();

	private Sandbox sandbox = null;

	public static String createClassNameByMeta(SandboxClass meta) {
		return meta.packageName() + "." + meta.className() + "/" + meta.version();
	}

	public AbstractSandboxWrapper(T t, Scriptable scope) {
		this.object = t;
		this.scope = scope;
		this.parentScope = scope.getParentScope();

		SandboxClass meta = this.getClass().getAnnotation(SandboxClass.class);

		if (this.getClass().isAnnotationPresent(SandboxClass.class)) {
			className = createClassNameByMeta(meta);
		} else {
			throw new RuntimeException(this.getClass().getName() + " does not have a SandboxClass annotation!");
		}

		reflector = new SandboxClassReflector(this);
	}

	/**
	 * constructor will be invoked when class is created via Framework.create(...) inside the sandbox
	 */
	public void constructor() {
	}

	public T getWrappedObject() {
		return object;
	}

	@Override
	public String toString() {
		return className;
	}

	@SandboxMethod(finalized = true)
	public final String getVersion() {
		return this.getClass().getAnnotation(SandboxClass.class).version();
	}

	final protected Sandbox getSandbox() {
		if (sandbox == null) {
			sandbox = (Sandbox) ((ScriptableObject) scope).getAssociatedValue("sandbox");
		}

		return sandbox;
	}

	protected Object callJsMethod(ScriptableObject object, String name, Object[] args) {
		return getSandbox().safeSandboxCall((Callable) object.get(name, object), object, args);
	}

	protected Object callJsMethod(ScriptableObject object, String name) {
		return callJsMethod(object, name, Context.emptyArgs);
	}

	/**
	 * tries to call a user or sandbox defined function stored in a property. can be overwritten.
	 *
	 * @param key
	 * @param scope
	 * @param args
	 * @return
	 */
	public Object callProperty(String key, Scriptable scope, Object[] args) {
		return getSandbox().safeSandboxCall(scope, (Callable) get(key, null), this, args);
	}

	public Object callProperty(String key, Object[] args) {
		return callProperty(key, args);
	}

	public Object callProperty(String key, Scriptable scope) {
		return callProperty(key, scope);
	}

	public Object callProperty(String key) {
		return callProperty(key, Context.emptyArgs);
	}

	/**
	 * checks if prototype is resolvable instance
	 */
	public static boolean isInstanceOf(Scriptable input, Class<?> base) {
		while ((input = input.getPrototype()) != null) {
			if (base.isAssignableFrom(input.getClass())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * checks if prototype is resolvable instance
	 */
	public boolean isInstanceOf(Class<?> base) {
		return isInstanceOf(this, base);
	}

	@Override
	public void delete(int arg0) {
		delete(Integer.toString(arg0));
	}

	@Override
	public void delete(String arg0) {
		overwrittenProperties.remove(arg0);
	}

	@Override
	public java.lang.Object get(String arg0, Scriptable arg1) {
		if (overwrittenProperties.containsKey(arg0) && !reflector.isMethodFinal(arg0)) {
			return overwrittenProperties.get(arg0);
		}

		if (reflector.hasMethod(arg0)) {
			return reflector.getMethod(arg0);
		}

		if (reflector.hasProperty(arg0)) {
			return reflector.getProperty(arg0);
		}

		return Scriptable.NOT_FOUND;
	}

	@Override
	public java.lang.Object get(int arg0, Scriptable arg1) {
		return get(Integer.toString(arg0), arg1);
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public Object getDefaultValue(Class<?> arg0) {
		if (arg0.isAssignableFrom(String.class)) {
			return toString();
		}

		return Context.getUndefinedValue();
	}

	@Override
	public Object[] getIds() {
		Object reflectorIds[] = reflector.getIds();

		int ridl = reflectorIds.length, pointer = 0;

		Object ids[] = new Object[ridl + overwrittenProperties.size()];

		System.arraycopy(reflectorIds, 0, ids, 0, ridl);

		for (String key : overwrittenProperties.keySet()) {
			ids[ridl + pointer++] = key;
		}

		return ids;
	}

	@Override
	public Scriptable getParentScope() {
		return parentScope;
	}

	@Override
	public Scriptable getPrototype() {
		return prototype;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return reflector.hasMethod(arg0);
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return has(Integer.toString(arg0), arg1);
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		return ScriptRuntime.jsDelegatesTo(instance, this);
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
		put(Integer.toString(arg0), arg1, arg2);
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		overwrittenProperties.put(arg0, arg2);
	}

	@Override
	public void setParentScope(Scriptable parentScope) {
		this.parentScope = parentScope;
	}

	@Override
	public void setPrototype(Scriptable prototype) {
		this.prototype = prototype;
	}
}
