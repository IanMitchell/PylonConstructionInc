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
	protected int x;
	protected int y;
	protected int rallyX;
	protected int rallyY;
	protected int status;
	protected Point homeChokePoint;

	public Squad() {
		status = IDLE;
		squad = new ArrayList<Unit>();
		enemies = new ArrayList<Unit>();
		stragglers = new ArrayList<Unit>();
		rallyX = 0;
		rallyY = 0;
		homeChokePoint = getClosestChokePoint(new Point(JavaBot.homePositionX, JavaBot.homePositionY));
	}
	
	public void update() {
		cleanSquad();
		if(squad.size() > 0) {
			updateSquadPos();
			setEnemies();
			if (status == IDLE) {
				if (squad.size() > 6) {
					status = ATTACKING;
				}
				else {
					move(getClosestChokePoint(getSquadCenter()));
				}
			}
			else if (status == ATTACKING) {
				if(combatSimulationScore() >= 1) {
					moveToRallyPoint(rallyX, rallyY);
				}
				else {
					status = RETREATING;
				}
			}
			else if (status == DEFENDING) {
				
			}
			else if (status == RETREATING) {
				move(homeChokePoint);
				if(combatSimulationScore() >= 1) {
					moveToRallyPoint(rallyX, rallyY);
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
			JavaBot.bwapi.move(unit.getID(), this.x, this.y);
		}
	}
	protected void moveToRallyPoint(int x, int y) {
		for(Unit unit : squad) {
			JavaBot.bwapi.attack(unit.getID(), x, y);
		}
		for(Unit unit : stragglers) {
			JavaBot.bwapi.move(unit.getID(), this.x, this.y);
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
			if(Math.abs(enemy.getX() - x) <= NEARBY_RADIUS && Math.abs(enemy.getY() - y) <= NEARBY_RADIUS) {
				enemies.add(enemy);
			}
		}
	}
	
	public void setRallyPoint(int x, int y) {
		rallyX = x;
		rallyY = y;
	}
	
	public int combatSimulationScore() {
		return squad.size() - enemies.size();
	}
	
	protected void updateSquadPos() {
		Point center = getSquadCenter();
		JavaBot.bwapi.drawCircle((int)center.getX(), (int)center.getY(), NEARBY_RADIUS, NEARBY_RADIUS, false, false);
		for(Unit straggler : stragglers) {
			if(Math.abs(straggler.getX() - center.getX()) <= NEARBY_RADIUS && Math.abs(straggler.getY() - center.getY()) <= NEARBY_RADIUS) {
				stragglers.remove(straggler);
				squad.add(straggler);
			}
		}
	}
	
	protected Point getSquadCenter() {
		x = 0;
		y = 0;
		for(Unit unit: squad) {
			x += unit.getX();
			y += unit.getY();
		}
		x = x / squad.size();
		y = y / squad.size();
		return new Point(x,y);
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
