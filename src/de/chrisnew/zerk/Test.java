package de.chrisnew.zerk;

import de.chrisnew.zerk.input.LocalInputCommand;
import de.chrisnew.zerk.math.Line2D;
import de.chrisnew.zerk.math.Rectangle;
import de.chrisnew.zerk.math.Vector2D;

public class Test {
	public static void init() {
		new LocalInputCommand("lt1", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				Line2D l1 = new Line2D(0, 4, 4, 0);
				Line2D l2 = new Line2D(0, 0, 4, 4);

				Console.info("l1: " + l1);
				Console.info("l2: " + l2);

				if (!l1.parallelTo(l2)) {
					Vector2D intersection = l1.getIntersection(l2);

					Console.info("intersection of l1 and l2 is at " + intersection.toString());

					if (!l1.isPointOnLine(intersection)) {
						Console.info("intersection is not on line l1");
					} else {
						Console.info("intersection is on line l1");
					}

					if (!l2.isPointOnLine(intersection)) {
						Console.info("intersection is not on line l2");
					} else {
						Console.info("intersection is on line l2");
					}
				} else {
					Console.info("parallel.");
				}
			}
		});

		new LocalInputCommand("lt2", new ConsoleCommand() {
			@Override
			public void call(String[] args) {
				Line2D l1 = new Line2D(0, 4, 4, 0);
				Line2D l2 = new Line2D(0, 0, 1, 1);

				Console.info("l1: " + l1);
				Console.info("l2: " + l2);

				if (!l1.parallelTo(l2)) {
					Vector2D intersection = l1.getIntersection(l2);

					Console.info("intersection of l1 and l2 is at " + intersection.toString());

					if (!l1.isPointOnLine(intersection)) {
						Console.info("intersection is not on line l1");
					} else {
						Console.info("intersection is on line l1");
					}

					if (!l2.isPointOnLine(intersection)) {
						Console.info("intersection is not on line l2");
					} else {
						Console.info("intersection is on line l2");
					}
				} else {
					Console.info("parallel.");
				}
			}
		});

		new LocalInputCommand("at1", new ConsoleCommand() {

			@Override
			public void call(String[] args) {
				Rectangle rectA = new Rectangle(new Vector2D(0, 0), new Vector2D(0, 10), new Vector2D(10, 0));
				Rectangle rectB = new Rectangle(new Vector2D(0, 0), new Vector2D(10, 0), new Vector2D(0, 10));

				Vector2D vecA = new Vector2D(2, 3);
				Vector2D vecB = new Vector2D(20, 30);

				if (rectA.isPointInArea(vecA)) {
					Console.info("vecA in rectA");
				} else {
					Console.info("vecA not in rectA");
				}

				if (rectA.isPointInArea(vecB)) {
					Console.info("vecB in rectA");
				} else {
					Console.info("vecB not in rectA");
				}

				if (rectB.isPointInArea(vecA)) {
					Console.info("vecA in rectA");
				} else {
					Console.info("vecA not in rectA");
				}

				if (rectB.isPointInArea(vecB)) {
					Console.info("vecB in rectB");
				} else {
					Console.info("vecB not in rectB");
				}
			}
		});
	}
}
