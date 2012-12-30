package de.chrisnew.zerk.game.ui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import de.chrisnew.zerk.client.Client;
import de.chrisnew.zerk.game.GameMap.TextSegment;
import de.chrisnew.zerk.game.entities.Book;

public class BookUI extends JFrame {
	private static final long serialVersionUID = 5232986801408631310L;

	private final Book book;

	private final JTextPane bookContentPane = new JTextPane();

	public BookUI(Book book) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(BookUI.class.getResource("zerk.png")));
		this.book = book;

		setSize(320, 200);

		setLocationRelativeTo(getRootPane());

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		bookContentPane.setEditable(false);
		scrollPane.setViewportView(bookContentPane);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnReader = new JMenu("Reader");
		menuBar.add(mnReader);

		JMenuItem mntmClose = new JMenuItem("Close");
		mntmClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		mnReader.add(mntmClose);


		loadData();


		setVisible(true);
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title + " - ZerkReader");
	}

	private void loadData() {
		TextSegment ts = Client.getGameMap().getTextSegmentById(book.getContentId());

		if (ts == null) {
			setTitle("Unreadable book");
		} else {
			setTitle(book.getTitle());
			bookContentPane.setText(ts.getContent());
		}
	}

	private static final HashMap<String, BookUI> bookUIs = new HashMap<>();

	public static void open(Book book) {
		if (bookUIs.containsKey(book.getName())) {
			BookUI bookUI = bookUIs.get(book.getName());

			bookUI.setVisible(true);
			bookUI.requestFocus();
		} else {
			bookUIs.put(book.getName(), new BookUI(book));
		}
	}
}
