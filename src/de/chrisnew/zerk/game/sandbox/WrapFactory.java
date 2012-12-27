/* (c) 2012 Smart Internet Solutions UG (haftungsbeschraenkt) */

package de.chrisnew.zerk.game.sandbox;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

// based on PrimitiveWrapFactory from rhino examples.
public class WrapFactory extends org.mozilla.javascript.WrapFactory {
	@Override
	public Object wrap(Context cx, Scriptable scope, Object obj, Class<?> staticType)
	{
		if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
			return obj;
		} else if (obj instanceof Character) {
			char[] a = { ((Character)obj).charValue() };
			return new String(a);
		}

		// CR: wrap *Maps
		if (obj instanceof Map) {
			return new MapScriptable((Map<?, ?>) obj);
		}

		return super.wrap(cx, scope, obj, staticType);
	}
}
