package de.chrisnew.zerk.game;

import java.util.HashMap;
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
	public class TextSegment {
		private String id, title, content;

		public TextSegment(String id, String title, String content) {
			setId(id);
			setTitle(title);
			setContent(content);
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}

	private final AtomicInteger entityIdCounter = new AtomicInteger();
	private final Sandbox sandbox = new Sandbox(this);

	private final AreaCollection areas = new AreaCollection();
	private final EntityCollection entities = new EntityCollection();
	private final WallCollection walls = new WallCollection();

	private final HashMap<String, TextSegment> textSegments = new HashMap<>();

	private String name = "unnamed";

	public GameMap() {
	}

	public GameMap(String mapName) {
		setName(mapName);
	}

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

	public void reset() {
		areas.clear();
		entities.clear();
		walls.clear();
	}

	public void load() {
		reset();

		// TODO
		loadFakeData();
	}

	public void load(String mapName) {
		setName(mapName);

		load();
	}

	public void save(String mapName) {
		setName(mapName);

		save();
	}

	public void save() {
		// TODO
	}

	private void loadFakeData() {
		PlayerSpawn ps = new PlayerSpawn();
		ps.setPosition(1, 1);
		addEntity(ps);

		addWall(new Wall(0, 2, 10, 2));

		Book b1 = new Book();
		b1.setPosition(1, 3);
		b1.setContentId("book1.text");
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

		addTextSegment(new TextSegment("book1.text", "Book 1", "Lorem Ipsum Dolor Sit Amet"));

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

	public TextSegment getTextSegmentById(String contentId) {
		return textSegments.get(contentId);
	}

	public void addTextSegment(TextSegment textSegment) {
		textSegments.put(textSegment.getId(), textSegment);
	}
}
