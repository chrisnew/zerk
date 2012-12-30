package de.chrisnew.zerk.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.chrisnew.zerk.Console;
import de.chrisnew.zerk.ConsoleCommand;
import de.chrisnew.zerk.game.EntityCollection;
import de.chrisnew.zerk.game.Game;
import de.chrisnew.zerk.game.GameMap;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.game.entities.Player;
import de.chrisnew.zerk.game.logic.Physics;
import de.chrisnew.zerk.input.LocalInputCommand;
import de.chrisnew.zerk.input.swing.GameWindow;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.net.CommandPacket.PacketClass;
import de.chrisnew.zerk.server.Server;
import de.chrisnew.zerk.server.Server.ServerState;

public class Client {

	public static enum ClientState {
		DISCONNECTED,
		CONNECTING,
		CONNECTED,
	}

	private static ClientState clientState = ClientState.DISCONNECTED;

	private static String clientServerAddress = "";

	public static ClientState getClientState() {
		return clientState;
	}

	public static void setClientState(ClientState clientState) {
		Client.clientState = clientState;

		if (GameWindow.hasGameWindow()) {
			GameWindow.updateCompleteClientState();
		}
	}

	public static boolean isClientState(ClientState state) {
		return clientState == state;
	}

	private static void cleanupAfterDisconnect() {
//		clientEntities.clear();
		LocalPlayer.cleanup();

		if (GameWindow.hasGameWindow()) {
			GameWindow.updateCompleteClientState();
		}
	}

	public static void disconnect() {
		if (isClientState(ClientState.CONNECTED)) {
			Console.debug("disconnecting gracefully...");

			sendCommandPacket(new CommandPacket(PacketClass.CLIENT_DISCONNECT));
		}

		setClientState(ClientState.DISCONNECTED);

		if (clientConnection != null && (!clientConnection.isClosed() || clientConnection.isConnected())) {
			clientConnection.close();

			Console.info("you are disconnected now.");
		}

		cleanupAfterDisconnect();
	}

	public static void init() {
		registerCommands();
	}

	private static void registerCommands() {
//		new LocalInputCommand("serverlist", new ConsoleCommand() {
//			@Override
//			public void call(String[] args) {
//
//			}
//		});

		new LocalInputCommand("start", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				Console.info("Starting the game.");

				int port = 21337;

				if (args.length > 1) {
					try {
						port = Integer.parseInt(args[1]);
					} catch (Exception e) {
						Console.warn("unable to parse port number: " + args[1]);
					}
				}

				Server.start(port);

				if (Server.isServerState(ServerState.ONLINE)) {
					Client.connect("[::1]:" + port);
				}
			}
		});

		new LocalInputCommand("connect", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				if (args.length == 1) {
					Console.info("Usage: " + args[0] + " <address>");

					return;
				}

				disconnect();

				if (args[1].matches("^\\[.*\\]:[0-9]+$")) {
					connect(args[1]);
				} else {
					connect(args[1] + ":21337");
				}
			}
		});

		new LocalInputCommand("disconnect", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				disconnect();
			}
		}, ClientState.CONNECTED);
	}

	private static DatagramSocket clientConnection = null;

	public static DatagramPacket sendCommandPacket(CommandPacket dp) {
		if (!isClientState(ClientState.DISCONNECTED)) {
			byte buf[] = dp.getBytes();

			DatagramPacket dgp = new DatagramPacket(buf, buf.length);

			try {
				clientConnection.send(dgp);
				Console.debug("sent DatagramPacket via clientConnection. length = " + buf.length);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return dgp;
		}

		return null;
	}

	private static final ConcurrentLinkedQueue<CommandPacket> networkBacklog = new ConcurrentLinkedQueue<>();

	private static int retryCounter = 0;
	private static int nextRetry = 0;

	private static void tryConnectLoop() {
		if (nextRetry-- < 0) {
			if (retryCounter > 3) {
				Console.error("Could not connect to " + clientServerAddress + "!");
				setClientState(ClientState.DISCONNECTED);
			} else if (retryCounter > 0) {
				Console.info("Still trying to connect...");
			}

			sendCommandPacket(new CommandPacket(PacketClass.CLIENT_CONNECT).writeString(LocalPlayer.getName()));

			retryCounter++;
			nextRetry = 100;
		}
	}

	private static void tryConnect() {
		retryCounter = 0;
		nextRetry = 0;
	}

	public static void connect(String address) {
		if (isClientState(ClientState.CONNECTED)) {
			disconnect();
		}

		Console.info("Trying to connect to " + address);
		clientServerAddress = address;

		try {
			int sep = address.lastIndexOf(':');
			int port = 21337;

			if (sep != -1) {
				port = Integer.parseInt(address.substring(sep + 1));
				address = address.substring(0, sep);
			}

			address = address.replaceAll("[\\[\\]]", "");

			SocketAddress sa = new InetSocketAddress(address, port);

			clientConnection = new DatagramSocket();
			clientConnection.connect(sa);
			clientConnection.setSoTimeout(1); // actually we need non-blocking here...

			setClientState(ClientState.CONNECTING);

			initTickers();

			tryConnect();
		} catch (IOException e) {
			setClientState(ClientState.DISCONNECTED);
			Console.error("Connect failed! " + e.getMessage());
		}
	}

	private static void initTickers() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("Client Ticker Loop");

				while (!isClientState(ClientState.DISCONNECTED)) {
					long startTime = new Date().getTime();

					byte b[] = new byte[16384];
					DatagramPacket p = new DatagramPacket(b, b.length);

					try {
						clientConnection.receive(p);

						networkBacklog.add(new CommandPacket(p.getData(),  p.getAddress(), p.getPort()));

						Console.debug("added CommandPacket to client backlog.");
					} catch (IOException e) {
					}

					try {
						tick();
					} catch(Throwable t) {
						Console.drop("bad error happend, dropping!");
						t.printStackTrace();
					}

					// now align loop back to a 60hz timeline.
					long timeDiff = new Date().getTime() - startTime;

					if (timeDiff < 16) {
						try {
							clientConnection.setSoTimeout((int) (16 - timeDiff));
						} catch (SocketException e) {
						}
					} else {
					}
				}
			}
		}).start();
	}

	protected static void tick() {
		if (isClientState(ClientState.CONNECTING)) {
			tryConnectLoop();
		}

		if (!isClientState(ClientState.DISCONNECTED)) {
			CommandPacket dp;

			while ((dp = networkBacklog.poll()) != null) {
				Console.debug("backlogged message");

				try {
					switch (dp.getPacketClass()) {
					case CLIENT_CONNECT:
						LocalPlayer.handleNewClient(dp);
						break;

					case CLIENT_SAY:
						int uuid = dp.readInteger();
						String name = LocalPlayer.getPlayerNameByUuid(uuid);

						if (name != null) {
							Console.info(name + ": " + dp.readString());
						}

						break;

					case CLIENT_CONNECT_RESPONSE:
						Console.debug("Connected to " + clientServerAddress);
						Client.loadMap(dp.readString());
						LocalPlayer.setPlayerUniqueId(dp.readInteger());
						setClientState(ClientState.CONNECTED);
//						LocalPlayer.setEntity((Player) dp.readEntity(clientEntities));
						LocalPlayer.setEntity((Player) dp.readEntity(getGameMap().getEntities()));

						for (int i = dp.readInteger(); i != 0; i--) {
							LocalPlayer.setPlayerNameByUuid(dp.readInteger(), dp.readString());
						}

						Game.startUI();
						break;

					case CLIENT_DISCONNECT:
						setClientState(ClientState.DISCONNECTED);
						cleanupAfterDisconnect();
						Console.info("Got disconnected, reason: " + dp.readString());
						break;

					case CLIENT_MESSAGE_UI_GAME:
						Console.info(dp.readString());
						break;

					case MESSAGE:
						Console.info("Message from server: " + dp.readString());
						break;

					case ENTITY_UPDATE_FULL:
						for (int i = dp.readInteger(); i != 0; i--) {
							BaseEntity entity = null;

//							if ((entity = dp.readEntity(clientEntities)) instanceof Player) {
							if ((entity = dp.readEntity(getGameMap().getEntities())) instanceof Player) {
								if (GameWindow.hasGameWindow()) {
									GameWindow.updateHelperViews();
								}
							}

							Console.debug("ENTITY_UPDATE_FULL, entity: " + entity + " #" + entity.getId() + " #" + entity.hashCode());
						}
						break;

					case ENTITY_REMOVE:
						Client.removeEntity(dp.readInteger());
						break;

					case NETWORK_PING:
						sendCommandPacket(new CommandPacket(PacketClass.NETWORK_PONG).writeLong(dp.readLong()));
						break;

					case CLIENT_DROP:
						String name1 = LocalPlayer.getPlayerNameByUuid(dp.readInteger());

						if (name1 != null) {
							Console.info(name1 + " dropped, reason: " + dp.readString());
						}
						break;

					default:
						// TODO: drop and out
						Console.warn("unknown packet class received.");
						break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (isClientState(ClientState.CONNECTED)) {
			Game.thinkUI();
		}
	}


	public static void removeEntity(int entityId) {
//		clientEntities.remove(entityId);
		getGameMap().removeEntityById(entityId);
	}

	public static void removeEntity(BaseEntity ent) {
		removeEntity(ent.getId());
	}

//	private static final HashMap<Integer, BaseEntity> clientEntities = new HashMap<>();

	public static EntityCollection getEntities() {
//		return new LinkedList<>(clientEntities.values());
//		return new LinkedList<>(getGameMap().getEntities()); //.values());
		return getGameMap().getEntities();
	}

	protected static final GameMap gameMap = new GameMap();

	public static GameMap getGameMap() {
		return gameMap;
	}

	private static void loadMap(String mapName) {
		gameMap.load(mapName);
//		clientEntities.clear();
//		clientEntities.putAll(gameMap.getEntities());
	}

	public static void receiveViaLoopback(CommandPacket dp) {
		networkBacklog.add(dp);
	}

	protected static Physics localPhysics = new Physics(gameMap);

	public static Physics getPhysics() {
		return localPhysics;
	}

}
