package de.chrisnew.zerk.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import de.chrisnew.zerk.console.Console;

public class NetChannel {
	public static class NetChannelException extends IOException {
		public NetChannelException(String string) {
			super(string);
		}

		private static final long serialVersionUID = -5048713320052422200L;
	}

	public static interface NetChannelSendImpl {
		public void send(byte[] bytes, int len, SocketAddress remoteAddress) throws IOException;
	}

	private static final byte[] VERSION = new byte[] {1, 0};

	private static class ReliableNetCommandPacket {
		private final CommandPacket cmd;
		private final int sequenceNumber;

//		private boolean acknowledged = false;

		public int getSequenceNumber() {
			return sequenceNumber;
		}

		public CommandPacket getCommandPacket() {
			return cmd;
		}

		public ReliableNetCommandPacket(int sequenceNumber, CommandPacket cmd) {
			this.cmd = cmd;
			this.sequenceNumber = sequenceNumber;
		}

//		public void acknowledge() {
//			acknowledged = true;
//		}
	}

	private final BlockingQueue<CommandPacket> networkBacklog =  new LinkedBlockingQueue<>();

	public BlockingQueue<CommandPacket> getNetworkBacklog() {
		return networkBacklog;
	}

//	private static DatagramChannel datagramChannel;

	private final NetChannelSendImpl netChannelSendImpl;

	public NetChannel(NetChannelSendImpl impl) {
		netChannelSendImpl = impl;
	}

	private static final AtomicInteger
		sendSqnr = new AtomicInteger(),
		recvSqnr = new AtomicInteger();

	private SocketAddress remoteAddress;

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public void sendCommandPacket(CommandPacket cmd) {
		sendPacket(sendSqnr.incrementAndGet(), cmd);
	}

	private final ConcurrentHashMap<Integer, ReliableNetCommandPacket> backupPoolSend = new ConcurrentHashMap<>();

	/**
	 * standard packet
	 * - sequence number
	 * - md5 of following CommandPacket
	 * - CommandPacket
	 */
	private static final byte RNCMD_STDPACK	= 1;

	/**
	 * reask for specific packet
	 * - count of sequence numbers
	 * - number 1, ...
	 */
	private static final byte RNCMD_REASK	= 2;

	/**
	 * acknowledge
	 * - sequence number
	 */
	private static final byte RNCMD_ACK		= 30;

	/**
	 * rejecting
	 * - sequence number
	 * - last known sequence number
	 */
	private static final byte RNCMD_NACK	= 31;

	private void sendReliableNetCommandPacket(ReliableNetCommandPacket rncmd) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);

			CommandPacket cmd = rncmd.getCommandPacket();

			byte b[] = cmd.getBytes();

			oos.writeByte(VERSION[0]);
			oos.writeByte(VERSION[1]);
			oos.writeByte(RNCMD_STDPACK);
			oos.writeInt(rncmd.getSequenceNumber());
			oos.write(calculateChecksum(b));
			oos.writeShort(b.length);
			oos.write(b);

			oos.flush();

			netChannelSendImpl.send(baos.toByteArray(), baos.size(), new InetSocketAddress(cmd.getRemoteAddress(), cmd.getRemotePort()));

			oos.close();
			baos.close();
		} catch(IOException e) {
			// TODO add to retry-list
		}
	}

	private void sendPacket(int sequenceNumber, CommandPacket cmd) {
		ReliableNetCommandPacket rncmd = new ReliableNetCommandPacket(sequenceNumber, cmd);

		backupPoolSend.put(sequenceNumber, rncmd);

		sendReliableNetCommandPacket(rncmd);
	}

	private byte[] calculateChecksum(byte[] input) {
		MessageDigest md5;

		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

		md5.reset();
		md5.update(input);

		return md5.digest();
	}

	public void recv(byte[] receiveByteBuffer, InetSocketAddress sa) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(receiveByteBuffer);
		ObjectInputStream ois = new ObjectInputStream(bais);

		byte vmajor = ois.readByte();
		byte vminor = ois.readByte();

		if ((vmajor * 256) + vminor < (VERSION[0] * 256) + VERSION[1]) {
			return;
		}

		byte type = ois.readByte();
		int rsqnr = ois.readInt();

		recvSqnr.incrementAndGet();

		if (rsqnr > recvSqnr.get()) {
			Console.warn("missing packets! rsqnr = " + rsqnr + ", recvSqnr = " + recvSqnr.get()); // TODO
			return;
		}

		switch (type) {
		case RNCMD_STDPACK:
			// md5
			byte[] md5 = new byte[16];
			ois.read(md5);

			int l = ois.readShort();

			if (l > (65536 - 2 - 4*4 - 4 - 1)) {
				throw new NetChannelException("recv(): invalid payload length");
			}

			byte b[] = new byte[l];

			ois.read(b);

			byte rmd5[] = calculateChecksum(b);
			boolean correct = true;

			for (int i = 0; i != 16; i++) {
				if (rmd5[i] != md5[i]) {
					correct = false;
					break;
				}
			}

			if (correct) {
				networkBacklog.add(new CommandPacket(b, sa.getAddress(), sa.getPort()));
			} else {
				Console.warn("forged packet received, md5 checksum is invalid.");
			}

			break;

		case RNCMD_REASK:
			// TODO
			break;

		case RNCMD_ACK:
			// TODO
			break;

		case RNCMD_NACK:
			// TODO
			break;

		default:
			Console.warn("NetChannel.recv(): unknown rncmd received.");
			break;
		}
	}
}
