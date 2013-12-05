package javabot.models;

import java.awt.Point;
import java.util.*;

import javabot.JavaBot;
import javabot.controllers.ArmyManager;
import javabot.util.BWColor;

public class Squad {
	private ArrayList<Unit> squad;
	protected ArrayList<Unit> enemies;
	protected ArrayList<Unit> stragglers;
	public static final int ATTACKING = 0;
	public static final int DEFENDING = 1;
	public static final int IDLE = 2;
	public static final int RETREATING = 3;
	protected static final int NEARBY_RADIUS = 400;
	protected int status;
	protected Point rallyPoint;
	protected Point squadCenter;
	protected Point homeChokePoint;
	protected Point lastOrderPoint;

	public Squad() {
		status = IDLE;
		squad = new ArrayList<Unit>();
		enemies = new ArrayList<Unit>();
		stragglers = new ArrayList<Unit>();
		homeChokePoint = getClosestChokePoint(new Point(JavaBot.homePositionX, JavaBot.homePositionY));
		rallyPoint = new Point(-1, -1);
		lastOrderPoint = new Point(0, 0);
		squadCenter = new Point(-1, -1);
	}
	
	public void update() {
		updateSquadPos();
		if(squad.size() > 0) {
			moveStragglers();
			setEnemies(NEARBY_RADIUS);
			int combatScore = combatSimScore();
			int newStatus = 0;
			Point latestOrder;
			
			if(squad.size() >= 2 && combatScore >= 1) {
				newStatus = ATTACKING;
				latestOrder = rallyPoint;
			}
			else {
				if(status != IDLE) {
					newStatus = RETREATING;
				}
				latestOrder = homeChokePoint;
			}
			if(status != newStatus && !lastOrderPoint.equals(latestOrder)) {
				JavaBot.bwapi.printText("New order");
				status = newStatus;
				lastOrderPoint = latestOrder;
				moveToRallyPoint(lastOrderPoint, status);
				if(status == ATTACKING) {
					JavaBot.bwapi.printText("New order: Attacking (" + lastOrderPoint.x + "," + lastOrderPoint.y + ")");
				}
				else if(status == DEFENDING) {
					JavaBot.bwapi.printText("New order: Defending (" + lastOrderPoint.x + "," + lastOrderPoint.y + ")");
				}
				else if(status == RETREATING) {
					JavaBot.bwapi.printText("New order: Retreating (" + lastOrderPoint.x + "," + lastOrderPoint.y + ")");
				}
			}
		}
	}
	
	public void act() {
		
	}
	
	protected void move(Point p) {
		for(Unit unit : squad) {
			JavaBot.bwapi.move(unit.getID(), p.x, p.y);
		}
		for(Unit unit : stragglers) {
			JavaBot.bwapi.move(unit.getID(), squadCenter.x, squadCenter.y);
		}
	}
	
	protected void moveToRallyPoint(Point p, int status) {
		for(Unit unit : squad) {
			if(status == ATTACKING) {
				JavaBot.bwapi.attack(unit.getID(), p.x, p.y);
			}
			else if(status == DEFENDING) {
				JavaBot.bwapi.move(unit.getID(), p.x, p.y);
			}
			else if(status == RETREATING) {
				JavaBot.bwapi.move(unit.getID(), p.x, p.y);
			}
		}
		for(Unit unit : stragglers) {
			JavaBot.bwapi.move(unit.getID(), squadCenter.x, squadCenter.y);
		}
	}
	
	protected void moveStragglers() {
		for(Unit unit : stragglers) {
			JavaBot.bwapi.move(unit.getID(), squadCenter.x, squadCenter.y);
		}
	}
	
	public void assignUnit(Unit unit) {
		if(squad.size() == 0) {
			squad.add(unit);
		}
		else {
			stragglers.add(unit);
		}
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int size() {
		return squad.size();
	}
	
	protected void setEnemies(int radius) {
		enemies.clear();
		for(Unit enemy : JavaBot.bwapi.getEnemyUnits()) {
			if (inRange(squadCenter, new Point(enemy.getX(), enemy.getY()), radius))
				enemies.add(enemy);
		}
	}
	
	public void setRallyPoint(Point p) {
		rallyPoint = (Point) p.clone();
	}
	
	public int combatSimScore() {
		return squad.size() - enemies.size();
	}
	
	protected void updateSquadPos() {
		squadCenter = getCenter(squad);
		JavaBot.bwapi.drawCircle(squadCenter.x, squadCenter.y, NEARBY_RADIUS, BWColor.GREEN, false, false);
		
		for(Unit straggler : stragglers) {
			if(straggler.isCompleted()) {
				if(inRange(squadCenter, new Point(straggler.getX(), straggler.getY()), NEARBY_RADIUS)) {
					JavaBot.bwapi.printText("Straggler added to Squad");
					stragglers.remove(straggler);
					squad.add(straggler);
					moveToRallyPoint(lastOrderPoint, status);
				}
			}
		}
		squadCenter = getCenter(squad);
	}
	
	/**
	 * Removes unit from squad. This is called when Unit destroy event is fired in JavaBot
	 * @param unitId id of unit
	 * @return id of removed unit
	 */
	public int removeUnit(int unitId) {
		int id = -1;
		for (Unit unit : squad)
			if (unit.getID() == unitId) {
				squad.remove(unit);
				id = unitId;
				break;
			}
		
		return id;
	}
	
	/**
	 * Returns center of units
	 * @param units
	 * @return
	 */
	public static Point getCenter(ArrayList<Unit> units) {
		int x = 0, y = 0;
		
		if(units.size() >= 1) {
			for(Unit unit: units) {
				x += unit.getX();
				y += unit.getY();
			}
			x = x / units.size();
			y = y / units.size();
		}
		return new Point(x,y);
	}

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
