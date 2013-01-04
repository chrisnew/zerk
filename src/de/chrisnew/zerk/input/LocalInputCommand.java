package de.chrisnew.zerk.input;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.chrisnew.zerk.Zerk;
import de.chrisnew.zerk.client.Client;
import de.chrisnew.zerk.client.Client.ClientState;
import de.chrisnew.zerk.console.Console;
import de.chrisnew.zerk.console.ConsoleCommand;

/**
 * @todo refactor to de.chrisnew.zerk.console
 * @author CR
 *
 */
public class LocalInputCommand {
	private final ConsoleCommand cmd;
	private final Client.ClientState clientState;

	public static void init() {
		new LocalInputCommand("help", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				Console.info("Available commands are ");

				String commands[] = LocalInputCommand.getAvailableCommands().keySet().toArray(new String[]{});

				Arrays.sort(commands);

				for (String name : commands) {
					Console.info(" " + name);
				}
			}
		});

		new LocalInputCommand("quit", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				Zerk.shutdown();
			}
		});

		new LocalInputCommand("exec", new ConsoleCommand() { // TODO: exec <filename>
			@Override
			public void call(String[] args) {
			}
		});
	}

	public LocalInputCommand(String name, ConsoleCommand function) {
		this(name, function, null);
	}

	public LocalInputCommand(String name, ConsoleCommand function, Client.ClientState state) {
		this.name = name;
		cmd = function;
		clientState = state;

		register();
	}

	private final String name;

	private final static HashMap<String, LocalInputCommand> localInputCommands = new HashMap<>();

	private void register() {
		LocalInputCommand.registerLocalInputCommand(this);
	}

	public String getName() {
		return name;
	}

	public Client.ClientState getClientState() {
		return clientState;
	}

	public void call(String args[]) {
		cmd.call(args);
	}

	public static void registerLocalInputCommand(LocalInputCommand localInputCommand) {
		localInputCommands.put(localInputCommand.getName(), localInputCommand);
	}

	/**
	 * get available commands (filtered to current client state)
	 *
	 * @todo cache values
	 */
	public static Map<String, LocalInputCommand> getAvailableCommands() {
		HashMap<String, LocalInputCommand> commands = new HashMap<>();

		for (LocalInputCommand lic : localInputCommands.values()) {
			ClientState cs = lic.getClientState();

			if (cs != null && !Client.isClientState(cs)) {
				continue;
			}

			commands.put(lic.getName(), lic);
		}

		return commands;
	}

	public static void runCommand(String input) {
		// TODO better splitting
		String inputParts[] = input.split(" ");

		if (inputParts.length > 0) {
			LocalInputCommand lic = LocalInputCommand.getAvailableCommands().get(inputParts[0]);

			if (lic == null || (lic.getClientState() != null && !Client.isClientState(lic.getClientState()))) { // FIXME: broken
				Console.info("Unknown Command \"" + inputParts[0] + "\".");
			} else {
				lic.call(inputParts);
			}
		}
	}

	public static void runCommandAsync(String input) {
		commandExecutionQueue.add(input);
	}

	private static final LinkedBlockingQueue<String> commandExecutionQueue = new LinkedBlockingQueue<>();

	static { // FIXME: make thread destroyable
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						String cmd = commandExecutionQueue.poll(60, TimeUnit.SECONDS);

						if (cmd != null) {
							runCommand(cmd);
						}
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}

	public static LocalInputCommand getCommandByName(String string) {
		return localInputCommands.get(string);
	}
}
