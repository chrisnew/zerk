package de.chrisnew.zerk.game;

import de.chrisnew.zerk.Console;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.server.WorldState;

public class Game {
	public static void startUI() {
		Console.info("\n >> You've started the game! <<\n");
		Console.info("Take a look around in the room by entering 'look'.");
		Console.info("You can pickup items like books by entering 'use'.");
		Console.info("Notes can contain quests you can collect and start.");
	}

	public static void thinkUI() {

	}

	public static void startLogic() {

	}

	public static void thinkLogic() {
		for (BaseEntity entity : WorldState.getEntityList()) {
			entity.think();
		}
	}
}
