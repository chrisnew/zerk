package de.chrisnew.zerk.game.ui;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.chrisnew.zerk.Zerk;
import de.chrisnew.zerk.client.Client;
import de.chrisnew.zerk.client.LocalPlayer;
import de.chrisnew.zerk.console.ConsoleCommand;
import de.chrisnew.zerk.game.entities.InventoryItem;
import de.chrisnew.zerk.input.LocalInputCommand;

public class GameWindow extends JFrame {
	protected final JTextArea consoleTextArea = new JTextArea();
	protected final JList<InventoryItem> inventoryList = new JList<>();

	protected void runCommand() {
		String cmd = textField.getText();
		textField.setText("");
		writeMessage("> " + cmd);
		LocalInputCommand.runCommandAsync(cmd);
	}

	public GameWindow() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				LocalInputCommand.runCommandAsync("quit");
			}
		});

		setResizable(false);
		setTitle("GameWindow");
		getContentPane().setLayout(null);

		setSize(840, 480);

		setLocationRelativeTo(getRootPane());

		textField = new JTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					runCommand();
				}
			}
		});
		textField.setBounds(10, 420, 504, 23);
		getContentPane().add(textField);
		textField.setColumns(10);

		JButton btnNewButton = new JButton("Send");
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runCommand();
			}
		});
		btnNewButton.setBounds(524, 420, 98, 23);
		getContentPane().add(btnNewButton);
		consoleTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
		consoleTextArea.setEditable(false);

		JScrollPane consoleTextAreaContainer = new JScrollPane(consoleTextArea);
		consoleTextAreaContainer.setBounds(10, 11, 612, 398);

		getContentPane().add(consoleTextAreaContainer);
		inventoryList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 2) {
		            int index = inventoryList.locationToIndex(e.getPoint());

		            inventoryList.getModel().getElementAt(index).activate();
		        }
			}
		});

		inventoryList.setBounds(632, 11, 190, 150);
		inventoryList.setModel(new DefaultListModel<InventoryItem>());
		getContentPane().add(inventoryList);
		questList.setBounds(632, 172, 190, 237);

		getContentPane().add(questList);
		lblCurrentQuestName.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCurrentQuestName.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrentQuestName.setBounds(632, 420, 190, 22);
		lblCurrentQuestName.setVisible(false);

		getContentPane().add(lblCurrentQuestName);

		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("zerk.png")));
	}

	private static final long serialVersionUID = 2491518906082872005L;
	private final JTextField textField;

	@Override
	public void setTitle(String title) {
		super.setTitle(title + " - ZERK v" + Zerk.VERSION);
	}

	public void writeMessage(String message) {
		consoleTextArea.append(message + "\n");
		consoleTextArea.setCaretPosition(consoleTextArea.getText().length());
	}

	private static GameWindow gameWindow = null;
	private final JList<Object> questList = new JList<Object>();
	private final JLabel lblCurrentQuestName = new JLabel("Unknown Game Mode");

	public static void init() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
		}

		JFrame.setDefaultLookAndFeelDecorated(true);

		gameWindow = new GameWindow();

//		gameWindow.setVisible(true);

		gameWindow.textField.requestFocusInWindow();

		new LocalInputCommand("clear", new ConsoleCommand() {

			@Override
			public void call(String[] args) {
				gameWindow.consoleTextArea.setText("");
			}
		});
	}

	public static void open() {
		if (hasGameWindow()) {
			getGameWindow().setVisible(true);
		}
	}

	public static boolean hasGameWindow() {
		return gameWindow != null;
	}

	public static GameWindow getGameWindow() {
		return gameWindow;
	}

	public static void updateCompleteClientState() {
		updateWindowTitle();
		updateHelperViews();
		updateGameMode();
	}

	public static void updateGameMode() {
		gameWindow.lblCurrentQuestName.setVisible(Client.getClientState() != Client.ClientState.DISCONNECTED);

		switch (LocalPlayer.getGameMode()) {
		case FREE:
			gameWindow.lblCurrentQuestName.setText("Free Mode");
			break;

		case QUEST:
			gameWindow.lblCurrentQuestName.setText("Quest Mode");
			break;
		}
	}

	public static void updateHelperViews() {
		DefaultListModel<InventoryItem> mdl = ((DefaultListModel<InventoryItem>) gameWindow.inventoryList.getModel());

		mdl.clear();

		List<InventoryItem> inventory = LocalPlayer.getInventory();

		if (inventory != null) {
			for (InventoryItem item : inventory) {
				mdl.addElement(item);
			}
		}
	}

	private static void updateWindowTitle() {
		switch (Client.getClientState()) {
		case DISCONNECTED:
			GameWindow.getGameWindow().setTitle("Disconnected");
			break;

		case CONNECTING:
			GameWindow.getGameWindow().setTitle("Trying to connect");
			break;

		case CONNECTED:
			GameWindow.getGameWindow().setTitle("Connected");
			break;

		default:
			break;
		}
	}
}
