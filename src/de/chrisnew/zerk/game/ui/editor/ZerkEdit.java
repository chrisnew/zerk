package de.chrisnew.zerk.game.ui.editor;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;

import de.chrisnew.zerk.ConsoleCommand;
import de.chrisnew.zerk.game.ui.GameWindow;
import de.chrisnew.zerk.input.LocalInputCommand;

public class ZerkEdit extends JFrame {
	private final MapEditorPane mapEditorPane = new MapEditorPane();

	public ZerkEdit() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(GameWindow.class.getResource("zerk.png")));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle("ZerkEdit");

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnEditor = new JMenu("Editor");
		menuBar.add(mnEditor);

		JMenuItem mntmNewMenuItem = new JMenuItem("Open Map...");
		mntmNewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mnEditor.add(mntmNewMenuItem);

		JMenuItem mntmSaveMap = new JMenuItem("Save");
		mntmSaveMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mnEditor.add(mntmSaveMap);

		JMenuItem mntmSaveAs = new JMenuItem("Save as...");
		mnEditor.add(mntmSaveAs);

		mnEditor.addSeparator();

		JMenuItem mntmRunMap = new JMenuItem("Play Map");
		mnEditor.add(mntmRunMap);

		mnEditor.addSeparator();

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mnEditor.add(mntmQuit);

		JMenu mnScript = new JMenu("Map Data");
		menuBar.add(mnScript);

		JMenuItem mntmOpenScriptEditor = new JMenuItem("Open Script Editor");
		mnScript.add(mntmOpenScriptEditor);

		JMenuItem mntmOpenTextEditor = new JMenuItem("Open Text Editor");
		mnScript.add(mntmOpenTextEditor);

		JMenu mnWindow = new JMenu("Window");
		menuBar.add(mnWindow);

		JMenuItem mntmShowAreaList = new JMenuItem("Show Area List");
		mnWindow.add(mntmShowAreaList);

		JMenuItem mntmShowEntityList = new JMenuItem("Show Entity List");
		mnWindow.add(mntmShowEntityList);

		JMenuItem mntmShowWallList = new JMenuItem("Show Wall List");
		mnWindow.add(mntmShowWallList);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		getContentPane().add(splitPane);

		JTree tree = new JTree();
		splitPane.setRightComponent(tree);

		splitPane.setLeftComponent(mapEditorPane);

		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

		setSize(800, 600);
	}

	private static final long serialVersionUID = 4909733019382741413L;

	public static void init() {
		new LocalInputCommand("editor", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				ZerkEdit editor = new ZerkEdit();
				editor.open();
			}
		});
	}

	public void open() {
		setVisible(true);
	}
}