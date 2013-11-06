package javabot.models;

import java.awt.Point;
import java.util.ArrayList;

import javabot.JavaBot;
import javabot.controllers.ArmyManager;
import javabot.controllers.ScoutManager;
import javabot.types.UnitType;
import javabot.types.UnitType.UnitTypes;

public class ScoutSquad extends Squad {
	private Unit scout;
	private static int DANGER = 4;
	private boolean baseFound = false;
	private Unit weakestWorker = null;
	private static int SCOUT_RADIUS = 200;
	protected Point rallyPoint;
	protected Point squadPoint;
	protected Point homeChokePoint;
	protected Point lastOrderPoint;
	
	public ScoutSquad(Unit scout) {
		status = IDLE;
		this.scout = scout;
		enemies = new ArrayList<Unit>();
		homeChokePoint = getClosestChokePoint(new Point(JavaBot.homePositionX, JavaBot.homePositionY));
		squadPoint = new Point(scout.getX(), scout.getY());
		rallyPoint = new Point(-1, -1);
		lastOrderPoint = new Point(-1, -1);
	}
	
	public void update() {
		if(scout.getHitPoints() > 0) {
			if(status == IDLE) {
				scout();
			}
			updateSquadPos();
			setEnemies();
			analyzeArea();
			if(baseFound && status != DANGER && weakestWorker != null) {
				//JavaBot.bwapi.printText("Harassing workers");
				harass(weakestWorker);
			}
			else if (status == DANGER) {
				//JavaBot.bwapi.printText("Dangerous: Scout retreating");
				JavaBot.bwapi.move(scout.getID(), JavaBot.homePositionX, JavaBot.homePositionY);
				status = RETREATING;
			}
			else if (status == RETREATING) {
				//notify javabot to reassign worker (back to resource manager)
				if (scout.getTypeID() == UnitTypes.Protoss_Probe.ordinal())
					JavaBot.reassignUnit(scout.getID(), ScoutManager.class.getSimpleName());
				
			}
		}
	}
	
	public void setEnemies() {
		enemies.clear();
		for(Unit enemy : JavaBot.bwapi.getEnemyUnits()) {
			if(enemy.getX() - squadPoint.x <= SCOUT_RADIUS && enemy.getY() - squadPoint.y <= SCOUT_RADIUS) {
				enemies.add(enemy);
			}
		}
	}
	
	private void harass(Unit unit) {
		JavaBot.bwapi.attack(scout.getID(), unit.getID());
	}
	
	private void analyzeArea() {
		int attackingWorkers = 0;
		status = IDLE;
		for(Unit enemy : enemies) {
			UnitType type = JavaBot.bwapi.getUnitType(enemy.getTypeID());
			if(type.isBuilding()) {
				if(type.getID() == UnitTypes.Terran_Command_Center.ordinal()) {
					JavaBot.bwapi.printText("Found main @ " + enemy.getX() + ", " + enemy.getY());
					ArmyManager.getInstance().setEnemyMain(enemy.getX(), enemy.getY());
					baseFound = true;
				}
			}
			else if(type.isWorker()) {
				if(enemy.isAttacking()) {
					attackingWorkers++;
				}
				if(weakestWorker == null) {
					weakestWorker = enemy;
				}
				if(enemy.getHitPoints() < weakestWorker.getHitPoints()) {
					weakestWorker = enemy;
				}
			}
			else {
				status = DANGER;
			}
		}
		if(attackingWorkers > 1) {
			status = DANGER;
		}
	}
	
	protected void updateSquadPos() {
		squadPoint.x = scout.getX();
		squadPoint.y = scout.getY();
		JavaBot.bwapi.drawCircle(squadPoint.x, squadPoint.y, SCOUT_RADIUS, SCOUT_RADIUS, false, false);
	}
	
	public void scout() {
		if(!scout.isMoving()) {
			for(BaseLocation base : ScoutManager.bases) {
				if(Math.abs(base.getX() - squadPoint.x) < 150 && Math.abs(base.getY() - squadPoint.y) < 150) {
					continue;
				}
				if(!ScoutManager.mainFound) {
					if(base.isStartLocation()) {
						JavaBot.bwapi.move(scout.getID(), base.getX(), base.getY());
					}
				}
				else {
					JavaBot.bwapi.move(scout.getID(), base.getX(), base.getY());
				}
			}
		}
	}

	public int getUnitId() {
		return scout.getID();
	}
}
