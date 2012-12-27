/* (c) 2012 Smart Internet Solutions UG (haftungsbeschraenkt) */

package de.chrisnew.zerk.game.sandbox;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import de.chrisnew.zerk.game.GameMap;
import de.chrisnew.zerk.game.sandbox.wrapper.ConsoleSandboxWrapper010;
import de.chrisnew.zerk.game.sandbox.wrapper.GameMapSandboxWrapper010;
import de.chrisnew.zerk.math.Line2D;
import de.chrisnew.zerk.math.Rectangle;
import de.chrisnew.zerk.math.Vector2D;

public class Sandbox {
	private final ScriptableObject globalScope;

	private static final WrapFactory defaultWrapFactory = new WrapFactory();

	final private static Pattern sandboxWrapperPattern = Pattern.compile("^.*SandboxWrapper[0-9]+$");
	final private static HashMap<String, Matcher> sandboxWrapperMatcherCache = new HashMap<String, Matcher> ();

	final private static ClassShutter classShutter = new ClassShutter() {
		@Override
		public boolean visibleToScripts(String className) {
			if (!sandboxWrapperMatcherCache.containsKey(className)) {
				sandboxWrapperMatcherCache.put(className, sandboxWrapperPattern.matcher(className));
			}

			return
				className.equals(Line2D.class.getName()) ||
				className.equals(Rectangle.class.getName()) ||
				className.equals(Vector2D.class.getName()) ||
				sandboxWrapperMatcherCache.get(className).matches();
		}
	};

	private static class SandboxContextFactory extends ContextFactory {
		@Override
		protected Context makeContext() {
			Context ctx = super.makeContext();

			ctx.setWrapFactory(defaultWrapFactory);
			ctx.setClassShutter(classShutter);

			return ctx;
		}
	}

	static {
		ContextFactory.initGlobal(new SandboxContextFactory());

		Context.enter();
	}

	public Sandbox(GameMap gameMap) {

		Context ctx = Context.enter();

		globalScope = ctx.initStandardObjects();

		globalScope.associateValue("sandbox", this);

		ScriptableObject.putConstProperty(globalScope, "Console", new ConsoleSandboxWrapper010(null, globalScope));
		ScriptableObject.putConstProperty(globalScope, "GameMap", new GameMapSandboxWrapper010(gameMap, globalScope));
	}

	public void loadScript(final String script) {
		ContextFactory.getGlobal().call(new ContextAction() {
			@Override
			public Object run(Context ctx) {
				return ctx.evaluateString(globalScope, script, "Script", 0, null);
			}
		});
	}

	public Object safeSandboxCall(Context ctx, final Scriptable scope, final Callable function, final Scriptable thisObj, final Object[] args) {
		if (ctx != null) {
			try {
				return function.call(ctx, scope, thisObj, args);
			} catch(EcmaError e) {
				throw new SandboxRuntimeException("Script error in " + e.sourceName() + " on line " + e.lineNumber(), e);
			}
		} else {
			return ContextFactory.getGlobal().call(new ContextAction() {
				@Override
				public Object run(Context arg0) {
					return safeSandboxCall(arg0, scope, function, thisObj, args);
				}
			});
		}
	}

	public Object safeSandboxCall(Scriptable scope, Callable function, Scriptable thisObj, Object[] args) {
		return safeSandboxCall(Context.getCurrentContext(), scope, function, thisObj, args);
	}

	public Object safeSandboxCall(Callable function, Scriptable thisObj, Object[] args) {
		return safeSandboxCall(globalScope, function, thisObj, args);
	}

	public Object safeSandboxCall(Callable function, Scriptable thisObj) {
		return safeSandboxCall(function, thisObj, Context.emptyArgs);
	}

	public Object safeSandboxCall(Callable function) {
		return safeSandboxCall(function, globalScope);
	}
}
