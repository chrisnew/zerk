package de.chrisnew.zerk.game;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.game.entities.Book;
import de.chrisnew.zerk.game.entities.Dog;
import de.chrisnew.zerk.game.entities.PlayerSpawn;
import de.chrisnew.zerk.game.sandbox.Sandbox;
import de.chrisnew.zerk.math.Line2D;
import de.chrisnew.zerk.math.Vector2D;

/**
 * GameMap is the hub for areas, entities and walls.
 *
 * @author CR
 *
 */
public class GameMap {
	private final AtomicInteger entityIdCounter = new AtomicInteger();
	private final Sandbox sandbox = new Sandbox(this);

	private final AreaCollection areas = new AreaCollection();
	private final EntityCollection entities = new EntityCollection();
	private final WallCollection walls = new WallCollection();

	private String name = "";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AreaCollection getAreas() {
		return areas;
	}

	public EntityCollection getEntities() {
		return entities;
	}

	public void load(String mapName) {
		areas.clear();
		entities.clear();

		setName(mapName);

		loadFakeData();
	}

	private void loadFakeData() {
		PlayerSpawn ps = new PlayerSpawn();
		ps.setPosition(1, 1);
		addEntity(ps);

		addWall(new Wall(0, 2, 10, 2));

		Book b1 = new Book();
		b1.setPosition(1, 3);
		addEntity(b1);

		Book b2 = new Book();
		b2.setPosition(15, 17);
		b2.setTitle("TestBook b2 static");
		b2.setName("book1");
		addEntity(b2);

		Dog d1 = new Dog();
		d1.setPosition(13, 13);
		addEntity(d1);

		Area a1 = new Area(new Vector2D(0, 0), new Vector2D(0, 5), new Vector2D(5, 0));
		a1.setAreaName("Home");
		addArea(a1);

//		getSandbox().loadScript("Console.info('test: ' + GameMap.getEntity('book1').getPosition());");
	}

	public void addArea(Area area) {
		area.setGameMap(this);
		areas.add(area);
	}

	public void addEntity(BaseEntity be) {
		addEntity(0, be);
	}

	public void addEntity(int entityId, BaseEntity be) {
		if (entityId == 0) {
			be.setId(createEntityId());
		}

		entities.add(be);
	}

	public void addWall(Wall wall) {
		walls.add(wall);
	}

	public void removeWall(Wall wall) {
		// TODO: clean up
		for (Line2D w : new LinkedList<Line2D>(walls)) {
			if (w.equals(wall)) {
				walls.remove(w);
			}
		}
	}

//	public boolean isVisibleFor(Vector2D beholderPosition, Vector2D objectPosition) { // FIXME
//		Line2D traceLine = new Line2D(beholderPosition, objectPosition);
//
//		for (Line2D w : walls) {
//			if (w.intersects(traceLine)) { // || w.isPointOnLine(objectPosition)) {
//				return false;
//			}
//		}
//
//		return true;
//	}
//
//	public boolean isVisibleFor(BaseEntity beholder, BaseEntity object) {
//		return isVisibleFor(beholder.getPosition(), object.getPosition());
//	}

	private int createEntityId() {
		return entityIdCounter.incrementAndGet();
	}

	public EntityCollection getEntitiesInArea(Area area) {
		EntityCollection ec = new EntityCollection();

		for (BaseEntity entity : getEntities()) {
			if (area.isPointInArea(entity.getPosition())) {
				ec.add(entity);
			}
		}

		return ec;
	}

	public Area getAreaByEntity(BaseEntity entity) {
		return getAreaByPoint(entity.getPosition());
	}

	public Area getAreaByPoint(Vector2D point) {
		for (Area area : areas) {
			if (area.isPointInArea(point)) {
				return area;
			}
		}

		return null;
	}

	public void removeEntityById(int entityId) {
		entities.remove(entityId);
	}

	public BaseEntity getEntityById(int id) {
		return entities.getEntityById(id);
	}

	public BaseEntity getEntityByName(String name) {
		for (BaseEntity entity : getEntities()) {
			if (entity.getName().equals(name)) {
				return entity;
			}
		}

		return null;
	}

	public boolean doesEntityExist(BaseEntity other) {
		return entities.hasEntityById(other.getId());
	}

	public WallCollection getWalls() {
		return walls;
	}

	public Sandbox getSandbox() {
		return sandbox;
	}
}
