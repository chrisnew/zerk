package de.chrisnew.zerk.game.sandbox.wrapper;

import org.mozilla.javascript.Scriptable;

import de.chrisnew.zerk.console.Console;
import de.chrisnew.zerk.game.sandbox.AbstractSandboxWrapper;
import de.chrisnew.zerk.game.sandbox.annotation.SandboxClass;
import de.chrisnew.zerk.game.sandbox.annotation.SandboxMethod;

@SandboxClass(className="Console", packageName="zerk", version = "0.1.0", singleton = true)
public class ConsoleSandboxWrapper010 extends AbstractSandboxWrapper<Console> {
	public ConsoleSandboxWrapper010(Console t, Scriptable scope) {
		super(t, scope);
	}

	@SandboxMethod
	public void info(String str) {
		Console.info(str);
	}

	@SandboxMethod
	public void debug(String str) {
		Console.debug(str);
	}

	@SandboxMethod
	public void warn(String str) {
		Console.warn(str);
	}

	@SandboxMethod
	public void error(String str) {
		Console.error(str);
	}
}
