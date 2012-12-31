package de.chrisnew.zerk.game.entities;

import de.chrisnew.zerk.game.entities.annotation.AttributeGetter;
import de.chrisnew.zerk.game.entities.annotation.AttributeSetter;
import de.chrisnew.zerk.game.entities.annotation.EntityInfo;
import de.chrisnew.zerk.game.ui.BookUI;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.server.WorldState;

@EntityInfo(description="Books can be picked up and stored in player's inventory.")
public class Book extends InventoryItem {
	private String title = "Unknown Book";
	private String contentId = "";

	@AttributeGetter("title")
	public String getTitle() {
		return title;
	}

	@AttributeSetter("title")
	public void setTitle(String title) {
		this.title = title;
	}

	@AttributeGetter("contentId")
	public String getContentId() {
		return contentId;
	}

	@AttributeSetter("contentId")
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	@Override
	public void serialize(CommandPacket dp) {
		super.serialize(dp);
		dp.writeString(getTitle());
		dp.writeString(getContentId());
	}

	@Override
	public void unserialize(CommandPacket dp) {
		super.unserialize(dp);
		setTitle(dp.readString());
		setContentId(dp.readString());
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
		BookUI.open(this);
	}

	@Override
	public String getInventoryName() {
		return getTitle() + " (" + getClassname() + ")";
	}
}
