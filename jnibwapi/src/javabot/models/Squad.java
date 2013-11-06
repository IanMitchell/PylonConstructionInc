package javabot.models;

import java.awt.Point;
import java.util.*;

import javabot.JavaBot;
import javabot.controllers.ArmyManager;

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
	protected Point squadPoint;
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
		squadPoint = new Point(-1, -1);
	}
	
	public void update() {
		cleanSquad();
		updateSquadPos();
		if(squad.size() > 0) {
			moveStragglers();
			setEnemies();
			int combatScore = combatSimScore();
			int newStatus = 0;
			Point latestOrder;
			if(squad.size() >= 2 && combatScore >= 1) {
				newStatus = ATTACKING;
				latestOrder = rallyPoint;
			}
			else {
				newStatus = RETREATING;
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
			JavaBot.bwapi.move(unit.getID(), squadPoint.x, squadPoint.y);
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
		}
		for(Unit unit : stragglers) {
			JavaBot.bwapi.move(unit.getID(), squadPoint.x, squadPoint.y);
		}
	}
	
	protected void moveStragglers() {
		for(Unit unit : stragglers) {
			JavaBot.bwapi.move(unit.getID(), squadPoint.x, squadPoint.y);
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
		cleanSquad();
		return squad.size();
	}
	
	protected void cleanSquad() {
		for(Unit unit : squad) {
			if(unit.getHitPoints() <= 0) {
				squad.remove(unit);
			}
		}
	}
	
	public void setEnemies() {
		enemies.clear();
		for(Unit enemy : JavaBot.bwapi.getEnemyUnits()) {
			if(Math.abs(enemy.getX() - squadPoint.x) <= NEARBY_RADIUS && Math.abs(enemy.getY() - squadPoint.y) <= NEARBY_RADIUS) {
				enemies.add(enemy);
			}
		}
	}
	
	public void setRallyPoint(int x, int y) {
		rallyPoint.x = x;
		rallyPoint.y = y;
	}
	
	public int combatSimScore() {
		return squad.size() - enemies.size();
	}
	
	protected void updateSquadPos() {
		getSquadCenter();
		JavaBot.bwapi.drawCircle(squadPoint.x, squadPoint.y, NEARBY_RADIUS, NEARBY_RADIUS, false, false);
		for(Unit straggler : stragglers) {
			if(straggler.isCompleted()) {
				if(Math.abs(straggler.getX() - squadPoint.x) <= NEARBY_RADIUS && Math.abs(straggler.getY() - squadPoint.y) <= NEARBY_RADIUS) {
					JavaBot.bwapi.printText("Straggler added to Squad");
					stragglers.remove(straggler);
					squad.add(straggler);
				}
			}
		}
		getSquadCenter();
	}
	
	protected void getSquadCenter() {
		if(squad.size() >= 1) {
			squadPoint.x = 0;
			squadPoint.y = 0;
			for(Unit unit: squad) {
				squadPoint.x += unit.getX();
				squadPoint.y += unit.getY();
			}
			squadPoint.x = squadPoint.x / squad.size();
			squadPoint.y = squadPoint.y / squad.size();
		}
		//return new Point(x,y);
	}

	/**
	 * Returns distance between the two points
	 * @param p1 first point
	 * @param p2 second point
	 * @return distance between the points
	 */
	protected double getDistance(Point p1, Point p2) {
		return Math.sqrt((p1.getX()-p2.getX())*(p1.getX()-p2.getX()) + (p1.getY()-p2.getY())*(p1.getY()-p2.getY())); 
	}
	
	protected Point getClosestChokePoint(Point p1) {
		Point closestChokePoint = new Point(JavaBot.homePositionX, JavaBot.homePositionY);
		double distance = Double.MAX_VALUE;
		
		//send squad to closest chokepoint
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
