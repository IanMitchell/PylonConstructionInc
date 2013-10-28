package javabot.models;

import java.util.*;
import javabot.JavaBot;

public class Squad {
	private ArrayList<Unit> squad;
	private ArrayList<Unit> enemies;
	public static final int ATTACKING = 0;
	public static final int DEFENDING = 1;
	public static final int IDLE = 2;
	public static final int RETREATING = 3;
	private static final int NEARBY_RADIUS = 400;
	private int x;
	private int y;
	private int rallyX;
	private int rallyY;
	private int status;

	public Squad() {
		status = IDLE;
		squad = new ArrayList<Unit>();
		enemies = new ArrayList<Unit>();
	}
	
	public void update() {
		cleanSquad();
		updateSquadPos();
		setEnemies();
		if(status != DEFENDING) {
			if(combatSimulationScore() >= 1) {
				moveToRallyPoint();
			}
		}
	}
	
	public void act() {
		
	}
	
	private void moveToRallyPoint() {
		for(Unit unit : squad) {
			JavaBot.bwapi.attack(unit.getID(), rallyX, rallyY);
		}
	}
	
	public void assignUnit(Unit unit) {
		squad.add(unit);
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
	
	private void cleanSquad() {
		for(Unit unit : squad) {
			if(unit.getHitPoints() <= 0) {
				squad.remove(unit);
			}
		}
	}
	
	public void setEnemies() {
		enemies.clear();
		for(Unit enemy : JavaBot.bwapi.getEnemyUnits()) {
			if(enemy.getX() - x <= NEARBY_RADIUS && enemy.getY() - y <= NEARBY_RADIUS) {
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
	
	private void updateSquadPos() {
		x = 0;
		y = 0;
		for(Unit unit: squad) {
			x += unit.getX();
			y += unit.getY();
		}
		x = x / squad.size();
		y = y / squad.size();
		JavaBot.bwapi.drawCircle(x, y, NEARBY_RADIUS, NEARBY_RADIUS, false, false);
	}
}
