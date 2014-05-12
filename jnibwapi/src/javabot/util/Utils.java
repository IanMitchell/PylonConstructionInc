package javabot.util;

import java.awt.Point;

import javabot.JavaBot;
import javabot.models.ChokePoint;

public class Utils {
	
	/**
	 * Returns distance between the two points
	 * @param p1 first point
	 * @param p2 second point
	 * @return distance between the points
	 */
	public static double getDistance(Point p1, Point p2) {
		return Math.sqrt((p1.getX()-p2.getX())*(p1.getX()-p2.getX()) + (p1.getY()-p2.getY())*(p1.getY()-p2.getY())); 
	}
	
	/**
	 * 
	 * @param p1 center of coordinate p1
	 * @param p2 center of coordinate p2
	 * @param radius radius of circle
	 * @return true if distance between p1 and p2 is within circle
	 */
	public static boolean inRange(Point p1, Point p2, int radius) {
		return getDistance(p1, p2) <= radius;
	}
	
	public static Point getClosestChokePoint(Point p1) {
		Point closestChokePoint = new Point(JavaBot.homePositionX, JavaBot.homePositionY);
		double distance = Double.MAX_VALUE;
		
		for (ChokePoint chokePoint : JavaBot.bwapi.getMap().getChokePoints()) {
			Point chokeCoords = new Point(chokePoint.getCenterX(), chokePoint.getCenterY());
			double chokeDistance = getDistance(p1, chokeCoords);
			if (getDistance(p1, chokeCoords) < distance) {
				distance = chokeDistance;
				closestChokePoint = chokeCoords;
			}
		}
		
		return closestChokePoint;
	}
}
