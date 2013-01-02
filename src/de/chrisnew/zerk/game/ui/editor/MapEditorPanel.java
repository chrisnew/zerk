package de.chrisnew.zerk.game.ui.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import de.chrisnew.zerk.game.Area;
import de.chrisnew.zerk.game.GameMap;
import de.chrisnew.zerk.game.Wall;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.math.Vector2D;

public class MapEditorPanel extends JPanel {
	private static final long serialVersionUID = -3805313706121161666L;

	private final GameMap gameMap;

	protected Point2D lastMousePosition = new Point2D.Float(0, 0);
	protected Point2D centerPosition = new Point2D.Float(0, 0);

	private class MepMouseListener extends MouseInputAdapter implements MouseListener, MouseWheelListener, MouseMotionListener {
		private boolean mouseDnDMode = false;
		private final int[] mouseDnDStart = {0, 0};

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			float bias = -e.getWheelRotation() * 2.f;
			float newzoom = zoom + bias;
			float factor = newzoom / zoom;

			if (newzoom < 0.1f) {
				return;
			}

			zoom = newzoom;

			// re-center viewport to mouse position
			double x = (-lastMousePosition.getX() * factor) + lastMousePosition.getX();
			double y = (-lastMousePosition.getY() * factor) + lastMousePosition.getY();

			viewableArea[0] = viewableArea[0] - (int) Math.round(x / zoom);
			viewableArea[1] = viewableArea[1] - (int) Math.round(y / zoom);

			// FIXME
			selectedObject = null;

			repaint();

			super.mouseWheelMoved(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {

			lastMousePosition.setLocation(e.getX(), e.getY());
			centerPosition.setLocation(lastMousePosition);

			final int delta = 5;
			int[] moveVector = {e.getX() - mouseDnDStart[0], e.getY() - mouseDnDStart[1]};

			if (mouseDnDMode && (Math.abs(moveVector[0]) > delta || Math.abs(moveVector[1]) > delta)) {
				if (SwingUtilities.isRightMouseButton(e)) {
					moveViewportBy((int) - Math.floor(moveVector[0] / zoom), (int) - Math.floor(moveVector[1] / zoom));
				} else if (SwingUtilities.isLeftMouseButton(e)) {
//					handleDnd(	(int) Math.floor(mouseDnDStart[0] / zoom), (int) Math.floor(mouseDnDStart[1] / zoom),
//								(int) Math.floor(e.getX() / zoom), (int) Math.floor(e.getY() / zoom)
//					);

					handleDnd(mouseDnDStart[0], mouseDnDStart[1], e.getX(), e.getY());
				}

				// FIXME
				selectedObject = null;
			} else if (SwingUtilities.isRightMouseButton(e)) {
				showContextMenu(e.getX(), e.getY());
			} else if (SwingUtilities.isLeftMouseButton(e)) {
				handleClick(e.getX(), e.getY());
			}

			repaint();

			mouseDnDMode = false;

			super.mouseReleased(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			lastMousePosition.setLocation(e.getX(), e.getY());

			mouseDnDStart[0] = e.getX();
			mouseDnDStart[1] = e.getY();

			mouseDnDMode = true;

			repaint();

			super.mousePressed(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			lastMousePosition.setLocation(e.getX(), e.getY());

			if (e.getClickCount() == 2) {
				handleDoubleClick(e.getX(), e.getY());
			}

			repaint();

			super.mouseClicked(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lastMousePosition.setLocation(e.getX(), e.getY());
			centerPosition.setLocation(lastMousePosition);

			repaint();

			super.mouseMoved(e);
		}
	}

	public void reset() {
		lastMousePosition.setLocation(0, 0);
		centerPosition.setLocation(0, 0);
		selectedObject = null;
		usableObjects.clear();
		for (EntityWindow ew : entityWindows.values()) {
			ew.close();
			ew.dispose();
		}
		gameMap.reset();
	}

	public MapEditorPanel(GameMap gameMap) {
		this.gameMap = gameMap;

		setBackground(Color.BLACK);

		MepMouseListener listener = new MepMouseListener();

		addMouseListener(listener);
		addMouseMotionListener(listener);
		addMouseWheelListener(listener);
	}

	private final HashMap<Integer, EntityWindow> entityWindows = new HashMap<>();

	protected void handleDoubleClick(int x, int y) {
		for (Map.Entry<Object, Rectangle2D> entry: usableObjects.entrySet()) {
			if (entry.getValue().contains(x, y)) {
				if (entry.getKey() instanceof Area) {
//					area = (Area) entry.getKey();
				} else if (entry.getKey() instanceof BaseEntity) {
					showEntityWindow((BaseEntity) entry.getKey());
				}

				break;
			}
		}
	}

	private void showEntityWindow(BaseEntity entity) {
		EntityWindow ew = entityWindows.get(entity.getId());

		if (ew == null) {
			ew = new EntityWindow(entity);

			ew.setLocationRelativeTo(this);
			entityWindows.put(entity.getId(), ew);
		}

		ew.setVisible(true);
		ew.requestFocus();
	}

	private JPopupMenu createPopupMenu(final int x, final int y) {
		JPopupMenu menu = new JPopupMenu();

		for (final Class<? extends BaseEntity> entityClass : BaseEntity.getEntityClasses()) {
			JMenuItem item = new JMenuItem("Create " + entityClass.getSimpleName());
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						BaseEntity entity = entityClass.newInstance();

						entity.setPosition((float) Math.floor(viewableArea[0] + x / zoom), (float) Math.floor(viewableArea[1] + y / zoom));

						gameMap.addEntity(entity);
					} catch (InstantiationException | IllegalAccessException e1) {
						e1.printStackTrace();
					}
				}
			});

			menu.add(item);
		}

		return menu;
	}

	private JPopupMenu createPopupMenuForEntity(int x, int y, final BaseEntity entity) {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem deleteItem = new JMenuItem("Delete Entity");
		deleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedObject = null;

				EntityWindow ew = entityWindows.get(entity.getId());

				if (ew != null) {
					ew.close();
					entityWindows.remove(entity.getId());
				}

				gameMap.removeEntityById(entity.getId());

				repaint();
			}
		});
		menu.add(deleteItem);

		return menu;
	}

	protected void showContextMenu(int x, int y) {
		@SuppressWarnings("unused")
		Area area = null;
		BaseEntity entity = null;

		for (Map.Entry<Object, Rectangle2D> entry: usableObjects.entrySet()) {
			if (entry.getValue().contains(x, y)) {
				if (entry.getKey() instanceof Area) {
					area = (Area) entry.getKey();
				} else if (entry.getKey() instanceof BaseEntity) {
					entity = (BaseEntity) entry.getKey();
				}

				break;
			}
		}

		JPopupMenu menu = null;

		if (entity != null) {
			menu = createPopupMenuForEntity(x, y, entity);
		} else {
			menu = createPopupMenu(x, y);
		}

		if (menu != null) {
			menu.show(this, x, y);
		}
	}

	protected void handleDnd(int fromX, int fromY, int toX, int toY) {
		Area area = null;
		BaseEntity entity = null;

		for (Map.Entry<Object, Rectangle2D> entry: usableObjects.entrySet()) {
			if (entry.getValue().contains(fromX, fromY)) {
				if (entry.getKey() instanceof Area) {
					area = (Area) entry.getKey();
				} else if (entry.getKey() instanceof BaseEntity) {
					entity = (BaseEntity) entry.getKey();
				}

				break;
			}
		}

		/**
		 * in game units
		 */
		Vector2D moveVector = new Vector2D(Math.round((toX - fromX) / zoom), Math.round((toY - fromY) / zoom));

		if (area != null) {
			// FIXME: fucked up
//			synchronized (area) {
//				area.setPointA(area.getPointA().add(moveVector));
//				area.setPointB(area.getPointB().add(moveVector));
//				area.setPointC(area.getPointC().add(moveVector));
//			}
		}

		if (entity != null) {
			entity.setPosition(entity.getPosition().add(moveVector));
		}
	}

	protected Map.Entry<Object, Rectangle2D> selectedObject = null;

	protected void handleClick(int x, int y) {
		for (Map.Entry<Object, Rectangle2D> entry: usableObjects.entrySet()) {
			if (entry.getValue().contains(x, y)) {
				selectedObject = entry;
				return;
			}
		}

		selectedObject = null;
	}

	/**
	 * game units
	 * @param i
	 * @param j
	 */
	protected void moveViewportBy(int i, int j) {
		viewableArea[0] = viewableArea[0] + i;
		viewableArea[1] = viewableArea[1] + j;
	}

	/**
	 * represents x1, y1, x2, y2 of viewable map objects in their ingame units
	 */
	private final int[] viewableArea = {0, 0, 1, 1};

	@Override
	public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setFont(new Font("Verdana", Font.TRUETYPE_FONT, 10));

        // update viewport rectangle
        viewableArea[2] = (int) (getWidth() / zoom - viewableArea[0]);
        viewableArea[3] = (int) (getHeight() / zoom - viewableArea[1]);

        // draw background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.BLACK);

        // first, paint what's already there
        usableObjects.clear();
        paintAreas(g2d);
        paintEntities(g2d);
        paintWalls(g2d);

        // paint markers for current action
        paintMousePointer(g2d);
        paintCurrentObjectInfo(g2d);

        // last and maybe the least, paint the summary line
        paintSummaryLine(g2d);
	}

	private float zoom = 20.f;

	public float getZoom() {
		return zoom / 20;
	}

	public void setZoom(float zoom) {
		this.zoom = zoom * 20;
	}

	private void paintEntityInfo(Graphics2D g2d, BaseEntity entity) {
		float x = entity.getPosition().getX() - viewableArea[0];
		float y = entity.getPosition().getY() - viewableArea[1];

        g2d.setFont(new Font("Verdana", Font.TRUETYPE_FONT, 11));
        g2d.setColor(Color.BLACK);

		int fh = g2d.getFontMetrics().getHeight();

		String name = entity.getName();

		g2d.drawString(entity.getClassname() + (!name.isEmpty() ? " (" + name + ")" : ""), (x + 1.5f) * zoom, y * zoom + fh);
	}

	private void paintCurrentObjectInfo(Graphics2D g2d) {
		if (selectedObject != null) {
			if (selectedObject.getKey() instanceof BaseEntity) {
				paintEntityInfo(g2d, (BaseEntity) selectedObject.getKey());
			}

			g2d.setColor(Color.BLUE);
			g2d.draw(selectedObject.getValue());
			g2d.setColor(Color.CYAN);
			g2d.fill(selectedObject.getValue());
		}
	}

	private void paintMousePointer(Graphics2D g2d) {
		StringBuilder coords = new StringBuilder();

		double x = Math.floor(lastMousePosition.getX() / zoom);
		double y = Math.floor(lastMousePosition.getY() / zoom);

		coords.append(x);
		coords.append('/');
		coords.append(y);

		g2d.setColor(Color.GRAY);
		g2d.drawRect((int) (x * zoom), (int) (y * zoom), (int) (1 * zoom), (int) (1 * zoom));
		g2d.setColor(Color.BLACK);
		g2d.drawString(coords.toString(), (int) lastMousePosition.getX() + 5, (int) lastMousePosition.getY() + 40);
	}

	private void paintSummaryLine(Graphics2D g2d) {
		StringBuilder summary = new StringBuilder();

        g2d.setFont(new Font("Verdana", Font.TRUETYPE_FONT, 12));
        g2d.setColor(Color.BLACK);

		summary.append(gameMap.getName());
		summary.append(" - ");
		summary.append("Zoom: ");
		summary.append(Math.round(getZoom() * 10.f) / 10.f);
		summary.append(", Areas: ");
		summary.append(gameMap.getAreas().size());
		summary.append(", Entities: ");
		summary.append(gameMap.getEntities().size());
		summary.append(", Walls: ");
		summary.append(gameMap.getWalls().size());

		g2d.drawString(summary.toString(), 10, getHeight() - 10); // g2d.getFontMetrics().getHeight());
	}

	private final HashMap<Object, Rectangle2D> usableObjects = new HashMap<>();

	private void paintAreas(Graphics2D g2d) {
		int fh = g2d.getFontMetrics().getHeight();

		for (de.chrisnew.zerk.game.Area area : gameMap.getAreas()) {
			float x = area.getPointA().getX() - viewableArea[0];
			float y = area.getPointA().getY() - viewableArea[1];
			float w = area.getPointD().getX() - viewableArea[0] - x;
			float h = area.getPointD().getY() - viewableArea[1] - y;

			Rectangle2D rect = new Rectangle2D.Float(zoom * x, zoom * y, zoom * w, zoom * h);

			if (rect.contains(lastMousePosition)) {
				g2d.setColor(Color.CYAN);
			} else {
				g2d.setColor(Color.LIGHT_GRAY);
			}

			g2d.fill(rect);
			g2d.setColor(Color.BLACK);
			g2d.draw(rect);

			usableObjects.put(area, rect);

			g2d.drawString(area.getAreaName(), zoom * x + 5, zoom * y + 5 + fh);
		}
	}

	private void paintEntities(Graphics2D g2d) {
		for (BaseEntity entity : gameMap.getEntities()) {
			float x = entity.getPosition().getX() - viewableArea[0];
			float y = entity.getPosition().getY() - viewableArea[1];

			Rectangle2D rect = new Rectangle2D.Float(zoom * x, zoom * y, zoom * 1, zoom * 1);

			if (rect.contains(lastMousePosition)) {
				g2d.setColor(Color.CYAN);
			} else {
				g2d.setColor(Color.GRAY);
			}

			g2d.fill(rect);
			g2d.setColor(Color.BLACK);
			g2d.draw(rect);

			usableObjects.put(entity, rect);
		}
	}

	private void paintWalls(Graphics2D g2d) {
		for (Wall wall : gameMap.getWalls()) {
			float x1 = wall.getStartPoint().getX() - viewableArea[0];
			float y1 = wall.getStartPoint().getY() - viewableArea[1];
			float x2 = wall.getEndPoint().getX() - viewableArea[0];
			float y2 = wall.getEndPoint().getY() - viewableArea[1];
			g2d.drawLine(Math.round(zoom * x1), Math.round(zoom * y1), Math.round(zoom * x2), Math.round(zoom * y2));

			// TODO usableObjects.put(wall, rect);
		}
	}

	public void save() {
		try {
			gameMap.save();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error while saving Map!", e.getClass().getSimpleName() + ": " + e.getMessage(), JOptionPane.ERROR_MESSAGE);

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load() {
		reset();

		try {
			gameMap.load();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error while loading Map!", e.getClass().getSimpleName() + ": " + e.getMessage(), JOptionPane.ERROR_MESSAGE);

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
