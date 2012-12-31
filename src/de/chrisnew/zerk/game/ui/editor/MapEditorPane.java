package de.chrisnew.zerk.game.ui.editor;

import java.awt.Color;

import javax.swing.JPanel;

import de.chrisnew.zerk.game.GameMap;

public class MapEditorPane extends JPanel {
	private static final long serialVersionUID = -3805313706121161666L;

	private static final GameMap gameMap = new GameMap();

	public MapEditorPane() {
		setBackground(Color.BLACK);

		gameMap.reset();
	}
}
