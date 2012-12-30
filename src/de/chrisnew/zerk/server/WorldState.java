package de.chrisnew.zerk.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.chrisnew.zerk.Console;
import de.chrisnew.zerk.game.GameMap;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.game.entities.InventoryItem;
import de.chrisnew.zerk.game.entities.PlayerSpawn;
import de.chrisnew.zerk.game.logic.Physics;
import de.chrisnew.zerk.math.Vector2D;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.net.CommandPacket.PacketClass;

public class WorldState {
	private static final Map<Integer, Player> playerList = new ConcurrentHashMap<>();

	public static Player getPlayerByCommandPacket(CommandPacket dp) {
		return getPlayerByRemoteAddress(dp.getRemoteAddress(), dp.getRemotePort());
	}

	public static Player getPlayerByRemoteAddress(InetAddress remoteAddress, int remotePort) {
		return getPlayerByRemoteAddress(new InetSocketAddress(remoteAddress, remotePort));
	}

	public static Player getPlayerByRemoteAddress(SocketAddress remoteAddress) {
		InetSocketAddress isa = (InetSocketAddress) remoteAddress;

		for (Player player : playerList.values()) {
			InetSocketAddress isa2 = (InetSocketAddress) player.getRemoteAddress();

			if (isa.getAddress().equals(isa2.getAddress()) && isa.getPort() == isa2.getPort()) {
				return player;
			}
		}

		return null;
	}

	public static Player getPlayerByEntity(BaseEntity entity) {
		for (Player player : playerList.values()) {
			if (player.getEntity().equals(entity)) {
				return player;
			}
		}

		return null;
	}

	public static void broadcastPacket(CommandPacket dp) {
		for (Player player : playerList.values()) {
			player.sendCommandPacket(dp);
		}
	}

	public static CommandPacket createEntityBulkUpdateDataPacket() {
		CommandPacket dp = new CommandPacket(PacketClass.ENTITY_UPDATE_FULL);
		List<BaseEntity> entities = getEntityList();

		dp.writeInteger(entities.size());

		for (BaseEntity entity : entities) {
			Console.debug(entity + " #" + entity.getId() + " #" + entity.hashCode());

			if (entity instanceof de.chrisnew.zerk.game.entities.Player) {
				List<InventoryItem> inv = ((de.chrisnew.zerk.game.entities.Player) entity).getInventory();
				Console.debug("inv.size() = " + inv.size());
				for (InventoryItem i : inv) {
					Console.debug(i.toString());
				}
			}

			dp.writeEntity(entity);
		}

		return dp;
	}

	public static CommandPacket createEntityUpdateDataPacket(BaseEntity ent) {
		return new CommandPacket(PacketClass.ENTITY_UPDATE_FULL).writeInteger(1).writeEntity(ent);
	}

	public static void handleClientConnect(CommandPacket dp) {
		Player player = null;
		try {
			String name = dp.readString();

			Vector2D position = null;

			for (BaseEntity entity : getEntityList()) {
				if (entity instanceof PlayerSpawn) {
					position = entity.getPosition();
				}
			}

			de.chrisnew.zerk.game.entities.Player playerEntity = new de.chrisnew.zerk.game.entities.Player();
			player = new Player(name, dp.getRemoteAddress(), dp.getRemotePort());

			player.setEntity(playerEntity);
			player.setName(name);

			if (position != null) {
				playerEntity.setPosition(position);
			}

			playerList.put(player.getPlayerUniqueId(), player);

			addEntity(playerEntity);

			Console.debug("[SERVER] " + name + " connected from " + player.getRemoteAddress() + ".");

			CommandPacket cdp = new CommandPacket(PacketClass.CLIENT_CONNECT_RESPONSE)
				.writeString(WorldState.getCurrentMapName())
				.writeInteger(player.getPlayerUniqueId())
				.writeEntity(playerEntity);

			cdp.writeInteger(playerList.size());

			for (Player p : playerList.values()) {
				cdp.writeInteger(p.getPlayerUniqueId());
				cdp.writeString(p.getName());
			}

			player.sendCommandPacket(cdp);

			broadcastPacket(new CommandPacket(PacketClass.CLIENT_CONNECT).writeInteger(player.getPlayerUniqueId()).writeString(name));

			player.sendCommandPacket(createEntityBulkUpdateDataPacket());
		} catch (IOException e) {
			Console.warn("client connect challenge failed.");

//			if (player != null) {
//				playerList.remove(player);
//			}
		}
	}

//	private static final HashMap<Integer, BaseEntity> entities = new HashMap<>();


	public static void addEntity(BaseEntity entity) {
		if (getGameMap().doesEntityExist(entity)) {
			Console.warn("Entity already in world!");

			return;
		}

		getGameMap().addEntity(entity.getId(), entity);

		entity.spawn();

		Console.debug("addEntity(): spawned " + entity.getClassname() + " to " + entity.getPosition());
	}

	private static final GameMap gameMap = new GameMap();

	public static synchronized void loadMap(String mapName) {
		gameMap.load(mapName);
	}

	public static String getCurrentMapName() {
		return gameMap.getName();
	}

	public static void dropPlayer(Player player, String reason) {
		playerList.remove(player.getPlayerUniqueId());

		removeEntity(player.getEntity());

		broadcastPacket(new CommandPacket(PacketClass.CLIENT_DROP).writeInteger(player.getPlayerUniqueId()).writeString(reason));
	}

	public static void handleClientDisconnect(CommandPacket dp) {
		Player player = getPlayerByRemoteAddress(dp.getRemoteAddress(), dp.getRemotePort());

		if (player != null) {
			dropPlayer(player, "Disconnected.");
		}
	}

	public static void removeEntity(BaseEntity entity) {
		getGameMap().removeEntityById(entity.getId());

		// let the clients know, that an entity has been removed
		broadcastPacket(new CommandPacket(PacketClass.ENTITY_REMOVE).writeInteger(entity.getId()));

		Console.debug("removed entity " + entity.getClassname() + " #" + entity.getId());
	}

	public static GameMap getGameMap() {
		return gameMap;
	}

	public static BaseEntity getEntityById(int id) {
//		return entities.get(id);
		return getGameMap().getEntityById(id);
	}

	public static List<BaseEntity> getEntityList() {
		return new LinkedList<>(getGameMap().getEntities());
	}

	protected static Physics physics = new Physics(gameMap);

	public static Physics getPhysics() {
		return physics;
	}

	public static void emitSoundToClients(Vector2D pos, float volume, String sound) {
		// TODO Auto-generated method stub
	}

	public static void doHeartbeat() {
		for (Player player : playerList.values()) {
			player.ping();

			if (player.isNetworkDead()) {
				dropPlayer(player, "Timed out.");
			}
		}
	}

	public static List<Player> getPlayerList() {
		return new LinkedList<>(playerList.values());
	}

	public static boolean isEntityInWorld(BaseEntity other) {
		return getGameMap().doesEntityExist(other);
	}

	public static HashMap<Integer, BaseEntity> getEntityListByType(Class<? extends BaseEntity> cls) {
		HashMap<Integer, BaseEntity> ents = new HashMap<>();

		for (BaseEntity entity : getEntityList()) {
			if (cls.isAssignableFrom(entity.getClass())) {
				ents.put(entity.getId(), entity);
			}
		}

		return ents;
	}
}
