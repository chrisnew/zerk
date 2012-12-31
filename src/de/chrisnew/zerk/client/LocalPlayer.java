package de.chrisnew.zerk.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.chrisnew.zerk.Console;
import de.chrisnew.zerk.ConsoleCommand;
import de.chrisnew.zerk.client.Client.ClientState;
import de.chrisnew.zerk.game.Area;
import de.chrisnew.zerk.game.EntityCollection;
import de.chrisnew.zerk.game.VisibleEntity;
import de.chrisnew.zerk.game.Wall;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.game.entities.InventoryItem;
import de.chrisnew.zerk.game.entities.Player;
import de.chrisnew.zerk.game.ui.GameWindow;
import de.chrisnew.zerk.input.LocalInputCommand;
import de.chrisnew.zerk.math.Line2D;
import de.chrisnew.zerk.math.Vector2D;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.net.CommandPacket.PacketClass;

public class LocalPlayer {
	protected static Player playerEntity = null;
	protected static String name = "Unnamed";

	protected static int playerUniqueId = 0;

	public static enum LocalGameMode {
		FREE,
		QUEST
	}

	protected static LocalGameMode gameMode = LocalGameMode.FREE;

	public static LocalGameMode getGameMode() {
		return gameMode;
	}

	public static void setGameMode(LocalGameMode gameMode) {
		LocalPlayer.gameMode = gameMode;
	}

	public static void init() {
		new LocalInputCommand("gogo", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				if (args.length == 1) {
					Console.info(args[0] + " <direction sequence>");
					Console.info(" are you familiar with 'go'?\n then concatenate the first letter of your directions to a sequence\n like nnws for north, north, west, south.");

					return;
				}

				Console.info("Walking..");

				for (int i = 0, j = args[1].length(); i != j; i++) {
					Vector2D direction = null;

					switch (args[1].charAt(i)) {
					case 'n':
						direction = new Vector2D(0, 1);
						break;

					case 'e':
						direction = new Vector2D(1, 0);
						break;

					case 's':
						direction = new Vector2D(0, -1);
						break;

					case 'w':
						direction = new Vector2D(-1, 0);
						break;
					}

					if (direction == null) {
						continue;
					}

					Vector2D newPos = playerEntity.getPosition().add(direction);

					// FIXME: actually we need to send the sequence to the server to evaluate if it's correct.
					if (Client.getPhysics().tryEntityWalkTo(playerEntity, newPos)) {
						Client.sendCommandPacket(new CommandPacket(PacketClass.ENTITY_C2S_NPC_WALKBY).writeInteger(playerEntity.getId()).writeVector2D(direction));
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}

						Console.info(".. " + newPos.toText());
					} else {
						Console.info("Oh, impassible way, stopping walking.");

						break;
					}
				}

				Console.info("done!");
			}
		}, ClientState.CONNECTED);

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

				if (Client.getPhysics().tryEntityWalkTo(playerEntity, newPos)) {
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
				EntityCollection entities = Client.getEntities();

				boolean usedSomething = false;

				for (BaseEntity entity : entities) {
					if (!entity.canUse()) {
						continue;
					}

					if (entity.getPosition().substract(playerEntity.getPosition()).length() <= 3 && Client.getPhysics().isAccessibleForUsage(playerEntity, entity)) {
						Client.sendCommandPacket(new CommandPacket(PacketClass.ENTITY_USE_ENTITY).writeEntity(entity));

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
				boolean sawSomething = false;

				Area area = Client.getGameMap().getAreaByEntity(playerEntity);

				EntityCollection entities = null;

				if (area != null) {
					Console.info("You are in " + area.getAreaName() + ".");

					entities = area.getEntities();
				} else {
					entities = Client.getEntities(); // all entities known in world.
				}

				// HACK: actually we need to let a circle intersect with all walls, let's just check all directions
				Vector2D pp = playerEntity.getPosition();
				Line2D pps[] = {
					new Line2D(pp, pp.add(new Vector2D(0, 3))),
					new Line2D(pp, pp.add(new Vector2D(3, 0))),
					new Line2D(pp, pp.add(new Vector2D(0, -3))),
					new Line2D(pp, pp.add(new Vector2D(-3, 0)))
				};

				for (Wall wall : Client.getGameMap().getWalls()) {
					for (Line2D _pp : pps) {
						if (_pp.intersects(wall)) {
							Vector2D intersection = wall.getIntersection(_pp);

							Console.info("There's a Wall in " + intersection.substract(pp).toTextRelatively() + ".");

							sawSomething = true;
						}
					}
				}

				for (BaseEntity entity : entities) {
					if (!(entity instanceof VisibleEntity) || (entity instanceof Player)) {
						continue;
					}

					Vector2D distance = entity.getPosition().substract(playerEntity.getPosition());

					if (Client.getPhysics().isAccessibleForUsage(playerEntity, entity)) {
						Console.info("There's a " + entity.getClassname() + " in " + distance.toTextRelatively() + ".");

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
