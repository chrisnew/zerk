package de.chrisnew.zerk;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.chrisnew.zerk.client.Client;
import de.chrisnew.zerk.client.LocalPlayer;
import de.chrisnew.zerk.console.Console;
import de.chrisnew.zerk.console.ConsoleVariable;
import de.chrisnew.zerk.game.Game;
import de.chrisnew.zerk.game.ui.GameWindow;
import de.chrisnew.zerk.game.ui.editor.ZerkEdit;
import de.chrisnew.zerk.input.LocalInput;
import de.chrisnew.zerk.input.LocalInputCommand;
import de.chrisnew.zerk.server.Server;

public class Zerk {
	public static final String VERSION = "0.1.0";

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

	private static final ConsoleVariable<Boolean> dedicated = new ConsoleVariable<>("dedicated", false);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Console.init();
		Console.parseCommandLine(args);

		boolean dedicated = Zerk.dedicated.getValue();

		if (!dedicated) {
			GameWindow.init();
		}

		LocalInputCommand.init();

		if (!dedicated) {
			LocalPlayer.init();
			Client.init();

			ZerkEdit.init();
		}

		Server.init();
		Game.init();

		Console.debug("Zerk v" + VERSION + " ready.");

		LocalInputCommand.runCommand("g_intro");

		// Test.init();

		if (dedicated) {
			LocalInput.waitForInput();
		} else {
			GameWindow.open();
		}
	}

	public static ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public static void shutdown() {
		Client.disconnect();
		Server.shutdown();
		System.exit(0);
	}
}
