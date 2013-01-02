package de.chrisnew.zerk.game.ui.editor;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.game.ui.GameWindow;

public class EntityWindow extends JFrame {
	private static final long serialVersionUID = 8000072316914826452L;
	private final JTextField entityName;
	private final JTable attributes;
	private final JTextField positionX;
	private final JTextField positionY;

	public EntityWindow(final BaseEntity entity) {
		setAlwaysOnTop(true);
		setType(Type.UTILITY);
		setIconImage(Toolkit.getDefaultToolkit().getImage(GameWindow.class.getResource("zerk.png")));
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		setTitle(entity.getClassname() + " #" + entity.getId());
		getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));

		setSize(200, 350);

		JLabel lblNewLabel = new JLabel("Entity Identifier");
		getContentPane().add(lblNewLabel, "2, 2, right, default");

		entityName = new JTextField();
		getContentPane().add(entityName, "4, 2, 3, 1, fill, default");
		entityName.setColumns(10);

		JLabel lblPosition = new JLabel("Position");
		getContentPane().add(lblPosition, "2, 4, right, default");

		positionX = new JTextField();
		getContentPane().add(positionX, "4, 4, fill, default");
		positionX.setColumns(10);

		positionY = new JTextField();
		getContentPane().add(positionY, "6, 4, fill, default");
		positionY.setColumns(10);

		JSeparator separator = new JSeparator();
		getContentPane().add(separator, "2, 6, 5, 1");

		attributes = new JTable();
		attributes.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		attributes.setFillsViewportHeight(true);

		Map<String, String> attrs = entity.getAttributes();

		final String valueMatrix[][] = new String[attrs.size()][];
		int i = 0;

		for (Map.Entry<String, String> attr : attrs.entrySet()) {
			valueMatrix[i++] = new String[] {attr.getKey(), attr.getValue()};
		}

		attributes.setModel(new DefaultTableModel(
			valueMatrix,
			new String[] {
				"Key", "Value"
			}
		) {
			private static final long serialVersionUID = 2059135249435491072L;
			Class<?>[] columnTypes = new Class<?>[] {
				String.class, String.class
			};
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, true
			};
			@Override
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		getContentPane().add(attributes, "2, 8, 5, 1, fill, fill");

		JSeparator separator_1 = new JSeparator();
		getContentPane().add(separator_1, "2, 10, 5, 1");

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel dtm = (DefaultTableModel) attributes.getModel();

				for (int i = 0; i != dtm.getRowCount(); i++) {
					String k = dtm.getValueAt(i, 0).toString();
					String v = dtm.getValueAt(i, 1).toString();

					entity.setAttribute(k, v);
				}

				entity.setName(entityName.getText());
				entity.setPosition(Float.parseFloat(positionX.getText()), Float.parseFloat(positionY.getText()));

				setVisible(false);
			}
		});
		getContentPane().add(btnSave, "4, 12, 3, 1, right, default");

		entityName.setText(entity.getName());
		positionX.setText(Float.toString(entity.getPosition().getX()));
		positionY.setText(Float.toString(entity.getPosition().getY()));
	}

	public void close() {
		setVisible(false);
		dispose();
	}
}
