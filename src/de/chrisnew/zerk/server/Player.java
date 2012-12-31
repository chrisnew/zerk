package de.chrisnew.zerk.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Date;

import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.net.CommandPacket.PacketClass;
import de.chrisnew.zerk.server.Server.ServerState;

public class Player {
	private String name;
	private SocketAddress remoteAddress;
	private de.chrisnew.zerk.game.entities.Player playerEntity;

	private int latency = -1;

	private final int playerUniqueId = (int) Math.round(Math.random() * 1000000);

	public Player(String name, InetAddress addr, int port) throws SocketException {
		setName(name);
		setRemoteAddress(addr, port);
	}

	public String getName() {
		return name;
	}

	public int getPlayerUniqueId() {
		return playerUniqueId;
	}

	public void setName(String name) {
		this.name = name;

		if (playerEntity != null) {
			playerEntity.setName(name);
		}
	}

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetAddress addr, int port) throws SocketException {
		this.remoteAddress = new InetSocketAddress(addr, port);
	}

	public void sendCommandPacket(CommandPacket dp) { //throws IOException {
		if (!Server.isServerState(ServerState.OFFLINE)) {
//			byte buf[] = dp.getBytes();

			dp.setRemoteAddress(getRemoteAddress());
			Server.sendCommandPacket(dp);

//			try {
//				dp.setRemoteAddress(getRemoteAddress());
//				Server.getServerChannel().send(ByteBuffer.wrap(buf), getRemoteAddress());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

	public void setEntity(de.chrisnew.zerk.game.entities.Player playerEntity) {
		this.playerEntity = playerEntity;
	}

	public de.chrisnew.zerk.game.entities.Player getEntity() {
		return this.playerEntity;
	}

	private Date lastPacketReceived = new Date();

	public void packetReceived() {
		lastPacketReceived = new Date();
	}

	public boolean isNetworkDead() {
		return new Date().getTime() - lastPacketReceived.getTime() > 30000;
	}

	public void ping() {
		sendCommandPacket(new CommandPacket(PacketClass.NETWORK_PING).writeLong(new Date().getTime()));
	}

	public int getLatency() {
		return latency;
	}

	public void setLatency(int latency) {
		this.latency = latency;
	}

	public void sendGameUiMessage(String string) {
		sendCommandPacket(new CommandPacket(PacketClass.CLIENT_MESSAGE_UI_GAME).writeString(string));
	}
}
