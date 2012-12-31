package de.chrisnew.zerk.game;

import de.chrisnew.zerk.Console;
import de.chrisnew.zerk.ConsoleCommand;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.input.LocalInputCommand;
import de.chrisnew.zerk.server.WorldState;

public class Game {
	public static void init() {
		new LocalInputCommand("g_intro", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				Console.info("Welcome to ZERK, please enter 'start' to start the game.");
			}
		});

		new LocalInputCommand("g_tutorial", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				Console.info("Take a look around in the room by entering 'look'.");
				Console.info("You can pickup items like books by entering 'use'.");
				Console.info("Notes can contain quests you can collect and start.");
			}
		});
	}

	public static void startUI() {
		Console.info("\n >> You've started the game! <<\n");

		LocalInputCommand.runCommand("g_tutorial");
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
