package de.chrisnew.zerk.net;

import java.net.SocketAddress;

public class NetChannel {
	private SocketAddress remoteAddress;

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public void sendCommandPacket(CommandPacket cmd) {

	}

//	public void sendOobPacket() {
//
//	}
}
