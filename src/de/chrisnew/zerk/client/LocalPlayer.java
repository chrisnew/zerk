package de.chrisnew.zerk.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.chrisnew.zerk.Console;
import de.chrisnew.zerk.ConsoleCommand;
import de.chrisnew.zerk.client.Client.ClientState;
import de.chrisnew.zerk.game.Area;
import de.chrisnew.zerk.game.VisibleEntity;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.game.entities.InventoryItem;
import de.chrisnew.zerk.game.entities.Player;
import de.chrisnew.zerk.input.LocalInputCommand;
import de.chrisnew.zerk.input.swing.GameWindow;
import de.chrisnew.zerk.math.Vector2D;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.net.CommandPacket.PacketClass;

public class LocalPlayer {
	protected static Player playerEntity = null;
	protected static String name = "Unnamed";

	protected static int playerUniqueId = 0;

	public static void init() {
		new LocalInputCommand("go", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				if (args.length == 1) {
					Console.info(args[0] + " <direction>");
					Console.info(" direction can be north, east, south or west.");

					return;
				}

				Vector2D direction;

				switch (args[1].toLowerCase()) {
				case "n":
				case "north":
					direction = new Vector2D(0, 1);
					break;

				case "e":
				case "east":
					direction = new Vector2D(1, 0);
					break;

				case "s":
				case "south":
					direction = new Vector2D(0, -1);
					break;

				case "w":
				case "west":
					direction = new Vector2D(-1, 0);
					break;

				default:
					Console.info("Sir, what weird kind of direction is " + args[1] + "?");
					return;
				}

				Vector2D newPos = playerEntity.getPosition().add(direction);

				if (Client.getPhysics().tryEntityWalkTo(playerEntity, newPos)) { // FIXME
					Client.sendCommandPacket(new CommandPacket(PacketClass.ENTITY_C2S_NPC_WALKBY).writeInteger(playerEntity.getId()).writeVector2D(direction));

					Console.info("I went to " + newPos.toText());
				} else {
					Console.info("This way is clearly impossible.");
				}
			}
		}, ClientState.CONNECTED);

		new LocalInputCommand("inv", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				List<InventoryItem> inv = playerEntity.getInventory();

				if (args.length == 1) {
					if (inv.size() == 0) {
						Console.info("I got nothing with me.");
					} else {
						int i = 1;
						for (InventoryItem item : inv) {
							Console.info(i++ + ". " + item.getInventoryName());
						}
					}
				} else {
					try {
						inv.get(Integer.parseInt(args[1]) - 1).activate();
					} catch(NumberFormatException | IndexOutOfBoundsException e) {
						Console.info("I don't have an item there.");
					}
				}
			}
		}, ClientState.CONNECTED);

		new LocalInputCommand("use", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				List<BaseEntity> entities = Client.getEntityList();

				boolean usedSomething = false;

				for (BaseEntity entity : entities) {
					if (!entity.canUse()) {
						continue;
					}

					if (entity.getPosition().substract(playerEntity.getPosition()).length() <= 3 && Client.getGameMap().isVisibleFor(playerEntity, entity)) {
						Client.sendCommandPacket(new CommandPacket(PacketClass.ENTITY_USE_ENTITY)/* .writeEntity(playerEntity)*/.writeEntity(entity));

						usedSomething = true;
					}
				}

				if (!usedSomething) {
					Console.info("There's nothing useful.");
				}
			}
		}, ClientState.CONNECTED);

		new LocalInputCommand("pos", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				Console.info("I'm at " + playerEntity.getPosition().toText() + ".");
			}
		}, ClientState.CONNECTED);

		new LocalInputCommand("look", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				boolean sawSomething = false, doRangeCheck = true;

				Area area = Client.getGameMap().getAreaByEntity(playerEntity);

				List<BaseEntity> entities = null;

				if (area != null) {
					Console.info("You are in " + area.getAreaName() + ".");

					entities = area.getEntities();
					doRangeCheck = false; // let's see user everything in area
				} else {
					entities = Client.getEntityList();
				}

				for (BaseEntity entity : entities) {
					if (!(entity instanceof VisibleEntity)) {
						continue;
					}

					if (entity.equals(playerEntity)) {
						continue;
					}

					// |posa - posb| = distance

					Vector2D distance = entity.getPosition().substract(playerEntity.getPosition());

					if ((!doRangeCheck || distance.length() < 5) && Client.getGameMap().isVisibleFor(playerEntity, entity)) { // TODO more visibility if light is on
						Console.info("There's a " + entity.getClassname() + " at " + distance.toText() + ".");
						sawSomething = true;
					}
				}

				if (!sawSomething) {
					Console.info("There's nothing interesting.");
				}
			}
		}, ClientState.CONNECTED);

		new LocalInputCommand("say", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				if (args.length > 1) {
					StringBuilder sb = new StringBuilder();

					for (int i = 1, l = args.length; i != l; i++) {
						sb.append(args[i]);
						sb.append(' ');
					}

					sb.deleteCharAt(sb.length() - 1);

					Client.sendCommandPacket(new CommandPacket(PacketClass.CLIENT_SAY_C2S).writeString(sb.toString()));
				} else {
					Console.info(args[0] + " <text ...>");

					return;
				}
			}
		}, ClientState.CONNECTED);

		new LocalInputCommand("status", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				Console.info("Health = " + playerEntity.getHealth());
			}
		}, ClientState.CONNECTED);

		new LocalInputCommand("name", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				if (args.length < 2) {
					Console.info("My name is '" + name  + "'.");
				} else {
					setName(args[1]);
				}
			}
		}, ClientState.CONNECTED);
	}

	private static final HashMap<Integer, String> playerUuidToName = new HashMap<>();

	public static void handleNewClient(CommandPacket dp) throws IOException {
		int uuid = dp.readInteger();
		String name = dp.readString();

		if (playerUniqueId != uuid) {
			Console.info(name + " connected!");
		}

		LocalPlayer.setPlayerNameByUuid(uuid, name);
	}

	public static void setPlayerNameByUuid(int uuid, String name) {
		playerUuidToName.put(uuid, name);
	}

	public static String getPlayerNameByUuid(int uuid) {
		return playerUuidToName.get(uuid);
	}

	public static void setEntity(Player entity) {
		playerEntity = entity;

		Console.debug("playerEntity: " + playerEntity + " #" + entity.getId() + " #" + entity.hashCode());
	}

	public static String getName() {
		return name;
	}

	public static void setName(String name) {
		LocalPlayer.name = name;
	}

	public int getPlayerUniqueId() {
		return playerUniqueId;
	}

	public static synchronized void setPlayerUniqueId(int playerUniqueId) {
		playerUuidToName.remove(playerUniqueId);

		LocalPlayer.playerUniqueId = playerUniqueId;

		playerUuidToName.put(playerUniqueId, getName());
	}

	public static List<InventoryItem> getInventory() {
//		if (playerEntity == null || playerEntity.getInventory().size() == 0) {
//			Console.warn("kdo");
//		}

		return playerEntity != null ? playerEntity.getInventory() : null;
	}

	public static void playerEntityUpdated() {
		if (GameWindow.hasGameWindow()) {
			GameWindow.updateHelperViews();
		}
	}

	public static void cleanup() {
//		Console.warn("cleanup bitch");
		playerEntity = null;

	}
}
