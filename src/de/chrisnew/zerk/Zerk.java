package de.chrisnew.zerk;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.chrisnew.zerk.client.Client;
import de.chrisnew.zerk.client.LocalPlayer;
import de.chrisnew.zerk.input.LocalInputCommand;
import de.chrisnew.zerk.input.swing.GameWindow;
import de.chrisnew.zerk.server.Server;

public class Zerk {
	public static final String VERSION = "0.1.0";

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Console.init();
		GameWindow.init();

		LocalInputCommand.init();

		LocalPlayer.init();

		Client.init();
		Server.init();

		Console.debug("Zerk v" + VERSION + " ready.");

		Test.init();

		Console.info("Welcome to ZERK, please enter 'start' to start the game.");
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
