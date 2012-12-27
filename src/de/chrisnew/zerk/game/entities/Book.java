package de.chrisnew.zerk.game.entities;

import de.chrisnew.zerk.Console;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.server.WorldState;

public class Book extends InventoryItem {
	private String title = "Unknown Book";

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void serialize(CommandPacket dp) {
		super.serialize(dp);
		dp.writeString(getTitle());
	}

	@Override
	public void unserialize(CommandPacket dp) {
		super.unserialize(dp);
		setTitle(dp.readString());
	}

	@Override
	public void use(BaseEntity user) {
		super.use(user);

		if (user instanceof Player) {
			WorldState.getPlayerByEntity(user).sendGameUiMessage("You picked up a book named '" + getTitle() + "'.");
		}
	}

	@Override
	public void activate() {
		Console.info("Activated Book locally.");
	}

	@Override
	public String getInventoryName() {
		return getTitle() + " (" + getClassname() + ")";
	}
}
