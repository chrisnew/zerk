package de.chrisnew.zerk.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.chrisnew.zerk.console.Console;

/**
 * @author CR
 *
 * dedicated console for server
 */
public class LocalInput {
	private static InputStreamReader localConsoleISR = new InputStreamReader(System.in);
	private static BufferedReader localConsoleReader = new BufferedReader(localConsoleISR);

	public static void waitForInput() {
		while (true) {
			try {
				System.err.print("> ");

				String input = localConsoleReader.readLine();

				if (input.length() == 0) {
					continue;
				}

				LocalInputCommand.runCommand(input);
			} catch(IOException e) {
				Console.error(e);
			}
		}
	}
}
