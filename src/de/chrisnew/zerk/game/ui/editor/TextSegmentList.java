package de.chrisnew.zerk.game.ui.editor;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.border.BevelBorder;

import de.chrisnew.zerk.game.GameMap;
import de.chrisnew.zerk.game.GameMap.TextSegment;
import de.chrisnew.zerk.game.ui.GameWindow;

public class TextSegmentList extends JFrame {
//	private final GameMap gameMap;

	public TextSegmentList(GameMap gameMap) {
//		this.gameMap = gameMap;

		setTitle("TextSegments");
		setResizable(false);

		setSize(250, 320);

		setIconImage(Toolkit.getDefaultToolkit().getImage(GameWindow.class.getResource("zerk.png")));

		getContentPane().setLayout(null);

		DefaultListModel<TextSegment> dlm = new DefaultListModel<TextSegment>();

		for (TextSegment ts : gameMap.getTextSegments()) {
			dlm.addElement(ts);
		}

		JList<TextSegment> list = new JList<>();
		list.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		list.setModel(dlm);
		list.setBounds(10, 11, 224, 243);
		getContentPane().add(list);

		JButton btnEdit = new JButton("Edit");
		btnEdit.setBounds(163, 265, 70, 23);
		btnEdit.setEnabled(false);
		getContentPane().add(btnEdit);

		JButton btnDelete = new JButton("Delete");
		btnDelete.setBounds(82, 265, 70, 23);
		btnDelete.setEnabled(false);
		getContentPane().add(btnDelete);

		JButton btnAdd = new JButton("Add");
		btnAdd.setBounds(11, 265, 60, 23);
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		getContentPane().add(btnAdd);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -3062300647452184138L;
}
