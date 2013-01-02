package de.chrisnew.zerk.game.sandbox.wrapper;

import org.mozilla.javascript.Scriptable;

import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.game.sandbox.AbstractSandboxWrapper;
import de.chrisnew.zerk.game.sandbox.UnknownSandboxWrapper;
import de.chrisnew.zerk.game.sandbox.annotation.SandboxClass;
import de.chrisnew.zerk.game.sandbox.annotation.SandboxMethod;
import de.chrisnew.zerk.math.Vector2D;

@SandboxClass(className="Entity", packageName="zerk", version = "0.1.0")
public class EntitySandboxWrapper010 extends AbstractSandboxWrapper<BaseEntity> {
	public EntitySandboxWrapper010(BaseEntity t, Scriptable scope) {
		super(t, scope);
	}

	@SandboxMethod
	public int getId() {
		return object.getId();
	}

	@SandboxMethod
	public Vector2D getPosition() {
		return object.getPosition();
	}

	@SandboxMethod
	public UnknownSandboxWrapper setPosition(float x, float y) {
// TODO

		return this;
	}

	@SandboxMethod
	public UnknownSandboxWrapper setAttribute(String key, String value) {
		object.setAttribute(key, value);

		return this;
	}

	@SandboxMethod
	public String getAttribute(String key) {
		return object.getAttribute(key);
	}
}
