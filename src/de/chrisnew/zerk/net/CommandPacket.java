package de.chrisnew.zerk.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import de.chrisnew.zerk.game.EntityCollection;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.math.Vector2D;

public class CommandPacket {
	public static enum PacketClass {
		/**
		 * CLIENT_CONNECT is the first packet when handshaking a new client
		 *
		 * data structure
		 *
		 *  string name
		 *
		 */
		CLIENT_CONNECT(1),
		CLIENT_CONNECT_RESPONSE(4),

		CLIENT_DISCONNECT(2),

		MESSAGE(3),

		ENTITY_REMOVE(5),
		ENTITY_UPDATE_FULL(6),

		/**
		 * walk npc relatively
		 *
		 * data structure
		 *  int entityId
		 *  vec2d adjustment
		 */
		ENTITY_C2S_NPC_WALKBY(7),

		/**
		 * emit sound
		 *
		 * data structure
		 *  vec2d position
		 *  float volume
		 *  string sound
		 */
		ENTITY_SOUND(8),

		/**
		 * user uses object
		 *
		 * data structure
		 *  entity user
		 *  entity object
		 */
		ENTITY_USE_ENTITY(9),

		CLIENT_SAY_C2S(10),
		CLIENT_SAY(11),

		NETWORK_PING(12),
		NETWORK_PONG(13), CLIENT_DROP(14), CLIENT_MESSAGE_UI_GAME(15), OOB_QUERY(16), OOB_QUERY_RESPONSE(17);

		private int val = 0;

		private PacketClass(int val) {
			this.val = val;
		}

		public int getValue() {
			return val;
		}

		public static PacketClass byValue(int b) {
			for (PacketClass pc : values()) {
				if (pc.val == b) {
					return pc;
				}
			}

			return null;
		}
	}

	private ByteArrayOutputStream baos = null;
	private ByteArrayInputStream bais = null;

	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;

	private PacketClass packetClass = null;

	private InetAddress remoteAddress;
	private int remotePort;

	public CommandPacket(PacketClass pc) {
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);

			setPacketClass(pc);

			oos.writeShort(pc.getValue());

			oos.flush();

			remoteAddress = null;
			remotePort = 65535;

		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (oos != null) {
			oos.close();
		}

		if (baos != null) {
			baos.close();
		}

		if (ois != null) {
			ois.close();
		}

		if (bais != null) {
			bais.close();
		}
	}

	public CommandPacket(byte[] data, InetAddress remoteAddress, int remotePort) throws IOException {
		bais = new ByteArrayInputStream(data);
		ois = new ObjectInputStream(bais);

		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;

		setPacketClass(PacketClass.byValue(ois.readShort()));
	}

	public long readLong() {
		try {
			return ois.readLong();
		} catch(IOException e) {
			return 0;
		}
	}

	public CommandPacket writeLong(long l) {
		try {
			oos.writeLong(l);
			oos.flush();
		} catch (IOException e) {
		}

		return this;
	}

	public int readInteger() {
		try {
			return ois.readInt();
		} catch(IOException e) {
			return 0;
		}
	}

	public CommandPacket writeInteger(int i) {
		try {
			oos.writeInt(i);
			oos.flush();
		} catch (IOException e) {
		}

		return this;
	}

	public byte readByte() {
		try {
			return ois.readByte();
		} catch(IOException e) {
			return 0;
		}
	}

	public CommandPacket writeByte(byte b) {
		try {
			oos.writeByte(b);
			oos.flush();
		} catch (IOException e) {
		}

		return this;
	}

	public float readFloat() {
		try {
			return ois.readFloat();
		} catch (IOException e) {
			return Float.NaN;
		}
	}

	public CommandPacket writeFloat(float f) {
		try {
			oos.writeFloat(f);
			oos.flush();
		} catch (IOException e) {
		}

		return this;
	}

	public String readString() {
		try {
			return ois.readUTF();
		} catch (Exception e) {
			return null;
		}
	}

	public CommandPacket writeString(String string) {
		try {
			oos.writeUTF(string);
			oos.flush();
		} catch (IOException e) {
		}

		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> readList(List<T> targetList) {
		try {
			for (int i = ois.readInt(); i != 0; i--) {
				targetList.add((T) ois.readObject());
			}
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		return targetList;
	}

	public CommandPacket writeList(List<?> list) {
		try {
			oos.writeInt(list.size());
			for (Object item : list) {
				oos.writeObject(item);
			}
			oos.flush();
		} catch (IOException e) {
		}

		return this;
	}

	public Vector2D readVector2D() {
		return new Vector2D(readFloat(), readFloat());
	}

	public CommandPacket writeVector2D(Vector2D v) {
		writeFloat(v.getX());
		writeFloat(v.getY());

		return this;
	}

	public BaseEntity readEntity() {
		return readEntity(null);
	}

	@SuppressWarnings("unchecked")
	public BaseEntity readEntity(EntityCollection updatableList) {
		try {
			String classname = readString();
			int id = readInteger();

			BaseEntity be = updatableList != null ? updatableList.getEntityById(id) : null; // updatableList.get(id) : null;

			if (be == null) {
				Class<? extends BaseEntity> entityClass = (Class<? extends BaseEntity>) Class.forName("de.chrisnew.zerk.game.entities." + classname);

				be = entityClass.newInstance();
				be.setId(id);

				if (updatableList != null) {
//					updatableList.put(id, be);
					updatableList.add(be);
				}
			}

			be.setHealth(readInteger());
			be.setPosition(readVector2D());

			be.unserialize(this);

			return be;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public CommandPacket writeEntity(BaseEntity entity) {
		writeString(entity.getClassname());
		writeInteger(entity.getId());
		writeInteger(entity.getHealth());
		writeVector2D(entity.getPosition());

		entity.serialize(this);

		return this;
	}

	public SimpleSerializable readSimpleSerializable(SimpleSerializable input) {
		input.unserialize(this);

		return input;
	}

	public CommandPacket writeSimpleSerializable(SimpleSerializable input) {
		input.serialize(this);

		return this;
	}

	public PacketClass getPacketClass() {
		return packetClass;
	}

	public void setPacketClass(PacketClass packetClass) {
		this.packetClass = packetClass;
	}

	public byte[] getBytes() {
		if (baos != null) {
			return baos.toByteArray();
		}

		return null;
	}

	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = ((InetSocketAddress) remoteAddress).getAddress();
		this.remotePort = ((InetSocketAddress) remoteAddress).getPort();
	}

	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * use this method only for testing usage
	 */
	public CommandPacket flip() {
		try {
			return new CommandPacket(getBytes(), getRemoteAddress(), getRemotePort());
		} catch (IOException e) {
			e.printStackTrace();

			return null;
		}
	}
}
