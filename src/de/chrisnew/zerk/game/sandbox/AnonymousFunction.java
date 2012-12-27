/* (c) 2012 Smart Internet Solutions UG (haftungsbeschraenkt) */

package de.chrisnew.zerk.game.sandbox;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public abstract class AnonymousFunction extends ScriptableObject implements Function {
	private static final long serialVersionUID = 1L;

	public static final String TEXT_TOSTRING = "function Function() { [native code] }";

	@Override
	final public Object getDefaultValue(Class<?> typeHint) {
		return toString();
	}

	@Override
	final public String getClassName() {
        return "Function";
    }

	@Override
	final public String toString() {
		return TEXT_TOSTRING;
	}

	@Override
	public Scriptable construct(Context ctx, Scriptable scope, Object[] args) {
		return null;
	}
}
