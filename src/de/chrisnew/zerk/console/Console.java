package de.chrisnew.zerk.console;

import de.chrisnew.zerk.client.Client;
import de.chrisnew.zerk.game.ui.GameWindow;

public class Console {
	private static void write(String message) {
		if (GameWindow.hasGameWindow()) {
			GameWindow.getGameWindow().writeMessage(message);
		} else {
			System.err.println(message);
		}
	}

	public static void debug(String message) {
//		write("DEBUG: " + message);
	}

	public static void info(String message) {
		write(message);
	}

	public static void warn(String message) {
		write("WARNING: " + message);
	}

	public static void error(Throwable t) {
		error(t.getMessage());
	}

	public static void error(String message) {
		write("ERROR: " + message);
	}

	public static void drop(String message) {
		error(message);
		Client.disconnect();
	}

	public static void init() {
		// TODO Auto-generated method stub

	}

	public static void fatal(String message) {
		write("FATAL: " + message);
		System.exit(1);
	}

	public static void fatal(String message, Throwable e) {
		fatal(message);
		e.printStackTrace();
	}

	public static void parseCommandLine(String[] args) {
		// TODO Auto-generated method stub

	}


}
