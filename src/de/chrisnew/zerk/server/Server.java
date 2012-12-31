package de.chrisnew.zerk.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.chrisnew.zerk.Console;
import de.chrisnew.zerk.ConsoleCommand;
import de.chrisnew.zerk.Zerk;
import de.chrisnew.zerk.game.Game;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.game.entities.InventoryItem;
import de.chrisnew.zerk.game.entities.NPC;
import de.chrisnew.zerk.game.entities.PlayerSpawn;
import de.chrisnew.zerk.input.LocalInputCommand;
import de.chrisnew.zerk.math.Vector2D;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.net.CommandPacket.PacketClass;
import de.chrisnew.zerk.net.NetChannel;
import de.chrisnew.zerk.net.NetChannel.NetChannelSendImpl;

public class Server {
	private static class ServerStartException extends Exception {
		public ServerStartException(String string) {
			super(string);
		}

		private static final long serialVersionUID = 7732452602122339364L;
	}

	public static enum ServerState {
		OFFLINE,
		ONLINE,
		WAITING
	}

	private static ServerState serverState = ServerState.OFFLINE;

	public static boolean isServerState(ServerState state) {
		return serverState == state;
	}

	public static ServerState getServerState() {
		return serverState;
	}

	public static void setServerState(ServerState serverState) {
		Server.serverState = serverState;
	}

	private static DatagramSocket serverSocket = null;

	private static final NetChannel netChannel = new NetChannel(new NetChannelSendImpl() {
		@Override
		public void send(byte[] bytes, int len, SocketAddress remoteAddress) throws IOException {
			channel.send(ByteBuffer.wrap(bytes, 0, len), remoteAddress);
		}
	});

	protected static DatagramChannel channel = null;
	protected static InetSocketAddress serverAddress = null;

	public static void initTickers() {
		// FIXME: we could merge those two threads into one

		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("Server Network Handler Loop");

				// handle network backlog
				CommandPacket dp = null;

				while (isServerState(ServerState.ONLINE)) {
					try {
						dp = netChannel.getNetworkBacklog().poll(16, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
					}

					if (dp == null) {
						continue;
					}

					Player serverPlayer = WorldState.getPlayerByCommandPacket(dp);

					if (dp.getPacketClass() != PacketClass.CLIENT_CONNECT) {
						if (serverPlayer == null) {
							continue;
						}

						serverPlayer.packetReceived();
					}

					switch (dp.getPacketClass()) {
					case CLIENT_CONNECT:
						if (serverPlayer == null) {
							WorldState.handleClientConnect(dp);
						}
						break;

					case CLIENT_DISCONNECT:
						WorldState.handleClientDisconnect(dp);
						break;

					case CLIENT_SAY_C2S: // TODO spam check
						WorldState.broadcastPacket(new CommandPacket(PacketClass.CLIENT_SAY).writeInteger(serverPlayer.getPlayerUniqueId()).writeString(dp.readString()));
						break;

					case ENTITY_C2S_NPC_WALKBY:
						NPC npc = (NPC) WorldState.getEntityById(dp.readInteger());
						Vector2D direction = dp.readVector2D();
						Vector2D newPos = npc.getPosition().add(direction);
						if (WorldState.getPhysics().tryEntityWalkTo(npc, newPos)) {
							npc.walkBy(direction);
							WorldState.broadcastPacket(WorldState.createEntityUpdateDataPacket(npc));
						}
						break;

					case ENTITY_SOUND:
						WorldState.emitSoundToClients(dp.readVector2D(), dp.readFloat(), dp.readString());
						break;

					case ENTITY_USE_ENTITY:
						BaseEntity user = serverPlayer.getEntity(), other = dp.readEntity();

						if (WorldState.isEntityInWorld(other)) {
							other.use(user);

							if (other instanceof InventoryItem) {
								WorldState.removeEntity(other);
							}
						} else {
							Console.warn("tried to use non-existent entity");
						}
						serverPlayer.sendCommandPacket(WorldState.createEntityUpdateDataPacket(user));
						break;

					case NETWORK_PONG:
						serverPlayer.setLatency((int) (new Date().getTime() - dp.readLong()));
						break;

					default:
						Console.debug("unknown PacketClass received, ignored.");
						break;
					}
				}
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				ByteBuffer receiveByteBuffer = ByteBuffer.allocate(32768);

				Thread.currentThread().setName("Server Network Loop");

				while (isServerState(ServerState.ONLINE)) {
					try {
						InetSocketAddress sa = (InetSocketAddress) channel.receive(receiveByteBuffer);

						if (sa != null && receiveByteBuffer.hasRemaining()) {
							receiveByteBuffer.flip();

							netChannel.recv(receiveByteBuffer.array(), sa);
						}

						receiveByteBuffer.clear();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
		}).start();
	}

//	public static void sendOobPacket(CommandPacket dp) {
//		try {
//			Server.getServerChannel().send(ByteBuffer.wrap(dp.getBytes()), new InetSocketAddress(dp.getRemoteAddress(), dp.getRemotePort()));
//		} catch (IOException e) {
//			// TODO put to retry list
//		}
//	}

//	protected static boolean tryHandleOobPacket(CommandPacket dp) {
//		switch (dp.getPacketClass()) {
//		case OOB_QUERY:
//			sendOobPacket(new CommandPacket(PacketClass.OOB_QUERY_RESPONSE)
//				.writeString(WorldState.getCurrentMapName())
//				.writeInteger(WorldState.getPlayerList().size())
//			);
//			return true;
//		default:
//			return false;
//		}
//	}

	public static void init() {
		setServerState(ServerState.OFFLINE);

		registerCommands();

		try {
			channel = DatagramChannel.open();
//			channel.configureBlocking(false);
		} catch (IOException e) {
			Console.fatal("could not create udp channel!", e);
		}

		Zerk.getScheduler().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!isServerState(ServerState.ONLINE)) {
					return;
				}

				try {
					tick();

					Game.thinkLogic();
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
		}, 0, 16, TimeUnit.MILLISECONDS);
	}

	private static void registerCommands() {
		new LocalInputCommand("sv_resent_el", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				entityFullSyncListTicker = 10;
			};
		});

		new LocalInputCommand("sv_entitylist", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				if (isServerState(ServerState.OFFLINE)) {
					return;
				}

				for (BaseEntity entity : WorldState.getEntityList()) {
					Console.info("#" + entity.getId() + ", " + entity.getClassname() + ", " + entity.getPosition());
				}
			}
		});

		new LocalInputCommand("sv_status", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				if (isServerState(ServerState.OFFLINE)) {
					Console.info("Server: offline.");
					return;
				}

				Console.info("Server: online, map = " + WorldState.getCurrentMapName());

				for (Player player : WorldState.getPlayerList()) {
					Console.info("#" + player.getPlayerUniqueId() + ", " + player.getName() + ", " + player.getEntity().getPosition() + ", " + player.getRemoteAddress() + ", " + player.getLatency() + "ms");
				}
			}
		});

		new LocalInputCommand("sv_start", new ConsoleCommand() {
			@Override
			public void call(String[] args) {  // TODO: default map
				int port = 21337;

				if (args.length > 2) {
					try {
						port = Integer.parseInt(args[2]);
					} catch (Exception e) {
						Console.warn("unable to parse port number: " + args[1]);
					}
				} else if (args.length > 1) {
					Console.info("Starting the game with map '" + args[1] + "'...");
					Server.start(port, args[1]);
				} else {
					Console.info(args[0] + " <mapname> [<port>]");
				}
			}
		});
	}

	private static int entityFullSyncListTicker = 10;
	private static int nextPlayerHeartbeatTicker = 100;

	/**
	 * will be called at a 60hz timeline
	 */
	protected static void tick() {
		// send all entities
		if (entityFullSyncListTicker-- < 0) {
			entityFullSyncListTicker = 2500;

			WorldState.broadcastPacket(WorldState.createEntityBulkUpdateDataPacket());

			Console.debug("sent complete list of entities to clients.");
		}

		if (nextPlayerHeartbeatTicker-- < 0) {
			nextPlayerHeartbeatTicker = 100;

			WorldState.doHeartbeat();

			Console.debug("player heartbeat ticker.");
		}
	}

	public static void start(int port, String mapname) {
		try {
			if (serverSocket == null) {
				serverSocket = channel.socket();
				serverAddress = new InetSocketAddress(port);
				serverSocket.bind(serverAddress);
				serverSocket.setBroadcast(true);
			}

			setServerState(ServerState.ONLINE);

			WorldState.loadMap(mapname);

			// uh, let's break down if there's no player spawn point.
			if (WorldState.getEntityListByType(PlayerSpawn.class).size() == 0) {
				throw new ServerStartException("PlayerSpawn missing in map");
			}

			Game.startLogic();

			initTickers();

			Console.info("Started server at [::]:" + port);
		} catch (IOException | ServerStartException e) {
			setServerState(ServerState.OFFLINE);

			Console.error(e);
		}
	}

	public static DatagramSocket getServerSocket() {
		return serverSocket;
	}

	public static void sendCommandPacket(CommandPacket cmd) {
		netChannel.sendCommandPacket(cmd);
	}

	public static void shutdown() {
		if (isServerState(ServerState.ONLINE)) {
			WorldState.broadcastPacket(new CommandPacket(PacketClass.CLIENT_DISCONNECT).writeString("Server shutting down."));
		}
	}

	public static InetSocketAddress getServerAddress() {
		return serverAddress;
	}
}
