package de.chrisnew.zerk.game.entities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.chrisnew.zerk.client.Client;
import de.chrisnew.zerk.game.entities.annotation.AttributeGetter;
import de.chrisnew.zerk.game.entities.annotation.AttributeSetter;
import de.chrisnew.zerk.math.Vector2D;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.net.CommandPacket.PacketClass;
import de.chrisnew.zerk.net.SimpleSerializable;

abstract public class BaseEntity implements SimpleSerializable, Comparable<BaseEntity> {
	private static final AtomicInteger entityIdCounter = new AtomicInteger();

	private int entityId = createEntityId();

	protected int health = 100;

	protected Vector2D position = new Vector2D();

	private String name = "";

	private static final Map<String, Class<? extends BaseEntity>> entityClasses = new HashMap<>();

	/*
	 * register all instantiatable entity classes here:
	 */
	static {
		entityClasses.put("Book", Book.class);
		entityClasses.put("Dog", Dog.class);
		entityClasses.put("PlayerSpawn", PlayerSpawn.class);
		entityClasses.put("Player", Player.class);
	}

	public static final Class<? extends BaseEntity> getEntityClassByClassname(String classname) {
		return entityClasses.get(classname);
	}

	public static final Collection<Class<? extends BaseEntity>> getEntityClasses() {
		return entityClasses.values();
	}

	public static enum State {
		DEAD(0), ALIVE(1);

		private int val = 0;

		private State(int val) {
			this.val = val;
		}

		public int getValue() {
			return val;
		}

		public static State byValue(int b) {
			for (State s : values()) {
				if (s.val == b) {
					return s;
				}
			}

			return null;
		}
	}

	public static int createEntityId() {
		return entityIdCounter.incrementAndGet();
	}

	private State state = State.ALIVE;

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}


	/**
	 * you cannot interact with dead entities
	 */
	final public boolean isAlive() {
		return getState() == State.ALIVE;
	}

	public void spawn() {
	}

	public void think() {
	}

	public boolean canUse() {
		return false;
	}

	public void use(BaseEntity other) {

	}

	public boolean canTalk() {
		return false;
	}

	public void talk(BaseEntity other) {

	}

	@AttributeGetter(value = "health", system = true)
	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	@AttributeSetter("health")
	public void setHealth(String health) {
		setHealth(Integer.parseInt(health));
	}

	public Vector2D getPosition() {
		return position;
	}

	public void setPosition(Vector2D position) {
		this.position = position;
	}

	public void setPosition(float x, float y) {
		setPosition(new Vector2D(x, y));
	}

	@AttributeGetter(value = "name", system = true)
	public String getName() {
		return name;
	}

	@AttributeSetter("name")
	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getAttributes() {
		return getAttributes(true);
	}

	public String getAttribute(String name) {
		Map<String, String> attrs = getAttributes();

		return attrs.get(name);
	}

	public Map<String, String> getAttributes(boolean skipSystemAttributes) {
		Map<String, String> attrs = new HashMap<>();

		for (Method method : getClass().getMethods()) {
			if (method.isAnnotationPresent(AttributeGetter.class)) {
				AttributeGetter meta = method.getAnnotation(AttributeGetter.class);

				if (meta.system()) {
					continue;
				}

				try {
					attrs.put(meta.value(), method.invoke(this, new Object[]{}).toString());
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					attrs.put(meta.value(), "");
				}
			}
		}

		return attrs;
	}

	public void setAttribute(String key, String value) {
		// FIXME: speed up
		for (Method method : getClass().getMethods()) {
			if (method.isAnnotationPresent(AttributeSetter.class)) {
				AttributeSetter meta = method.getAnnotation(AttributeSetter.class);

				if (meta.value().equalsIgnoreCase(key)) {
					try {
						method.invoke(this, new Object[]{value});
					} catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
					}
				}
			}
		}
	}

	@AttributeGetter(value = "id", system = true)
	final public int getId() {
		return entityId;
	}

	final public void setId(int entityId) {
		this.entityId = entityId;
	}

	final public String getClassname() {
		return getClass().getSimpleName();
	}

	public boolean equals(BaseEntity obj) {
		return getId() == obj.getId();
	}

	final protected void emitSound(float volume, String sound) {
		Client.sendCommandPacket(
			new CommandPacket(PacketClass.ENTITY_SOUND)
				.writeVector2D(getPosition())
				.writeFloat(volume)
				.writeString(sound)
		);
	}

	@Override
	public void serialize(CommandPacket dp) {
		dp.writeString(getName());
	}

	@Override
	public void unserialize(CommandPacket dp) {
		setName(dp.readString());
	}

	@Override
	public String toString() {
		return getClassname();
	}

    @Override
	public int compareTo(BaseEntity o) {
    	if (o.getId() == getId()) {
    		return 0;
    	}

    	return o.getId() > getId() ? -1 : 1;
    }
}
