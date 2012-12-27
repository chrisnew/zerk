package de.chrisnew.zerk.game.sandbox.wrapper;

import org.mozilla.javascript.Scriptable;

import de.chrisnew.zerk.game.GameMap;
import de.chrisnew.zerk.game.sandbox.AbstractSandboxWrapper;
import de.chrisnew.zerk.game.sandbox.annotation.SandboxClass;
import de.chrisnew.zerk.game.sandbox.annotation.SandboxMethod;

@SandboxClass(className="GameMap", packageName="zerk", version = "0.1.0")
public class GameMapSandboxWrapper010 extends AbstractSandboxWrapper<GameMap> {
	public GameMapSandboxWrapper010(GameMap t, Scriptable scope) {
		super(t, scope);
	}

	@SandboxMethod
	public EntitySandboxWrapper010 getEntity(String name) {
		return new EntitySandboxWrapper010(object.getEntityByName(name), scope);
	}
}
