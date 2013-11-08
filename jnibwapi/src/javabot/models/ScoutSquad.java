package javabot.models;

import java.awt.Point;
import javabot.JavaBot;
import javabot.controllers.ArmyManager;
import javabot.controllers.ScoutManager;
import javabot.types.UnitType;
import javabot.types.UnitType.UnitTypes;
import javabot.util.BWColor;

public class ScoutSquad extends Squad {
	public Unit scout;
	private static int DANGER = 4;
	private static int SCOUTING = 5;
	private Unit weakestWorker = null;
	private static int DANGER_RADIUS = 75;
	private static int SCOUT_RADIUS = 300;
	private int idleCount=0;
	
	public ScoutSquad(Unit scout) {
		super();
		this.scout = scout;
		squadCenter = new Point(scout.getX(), scout.getY());
	}
	
	@Override
	public void update() {
		if(scout.getHitPoints() > 0) {
			if(status == IDLE) {
				status = SCOUTING;
				scout();
			}
			
			updateSquadPos();
			setEnemies(SCOUT_RADIUS);
			status = analyzeArea();
			if (!ScoutManager.mainFound)
				status = SCOUTING;
			
			//idleCount is used because right before it scouts, it goes into idle. 
			if ((status == IDLE || scout.isIdle()) && ++idleCount > 1) {
				JavaBot.reassignUnit(scout.getID(), ScoutManager.class.getSimpleName());
			}
			if(status ==  ATTACKING) {
				harass(weakestWorker.getID());
			}
			if (status == DANGER) {
				JavaBot.bwapi.move(scout.getID(), JavaBot.homePositionX, JavaBot.homePositionY);
				status = RETREATING;
			}
			if (status == RETREATING) {
				//reassign probe back to resource manager if we are closer to our homebase than to enemy 
				if ((inRange(squadCenter, new Point(JavaBot.homePositionX, JavaBot.homePositionY), 400) && 
				 scout.getTypeID() == UnitTypes.Protoss_Probe.ordinal())) {
					JavaBot.reassignUnit(scout.getID(), ScoutManager.class.getSimpleName());
					scout = null;
				}
			}
		}
	}
	
	
	private void harass(int enemyId) {
		JavaBot.bwapi.attack(scout.getID(), enemyId);
	}
	
	private int analyzeArea() {
		int attackingWorkers = 0;
		
		for(Unit enemy : enemies) {
			UnitType type = JavaBot.bwapi.getUnitType(enemy.getTypeID());
			if(type.isBuilding()) {
				if(type.getID() == UnitTypes.Terran_Command_Center.ordinal() || 
				 type.getID() == UnitTypes.Protoss_Nexus.ordinal() ||
				 type.getID() == UnitTypes.Zerg_Lair.ordinal() ||
				 type.getID() == UnitTypes.Zerg_Hatchery.ordinal()) {
					ArmyManager.getInstance().setEnemyMain(enemy.getX(), enemy.getY());
					ScoutManager.mainFound = true; 
				}
			}
			else if(type.isWorker()) {
				if (isStronger(enemy) || scout.getHitPoints() < 10)
					return DANGER;
				else if (enemy.isConstructing()) {
					weakestWorker = enemy;
					return ATTACKING;
				}
				else if (weakestWorker != null && !isStronger(enemy) && inRange(squadCenter, new Point(enemy.getX(), enemy.getY()), DANGER_RADIUS))
					weakestWorker = enemy;
				else if(inRange(squadCenter, new Point(enemy.getX(), enemy.getY()), DANGER_RADIUS) && ++attackingWorkers > 1)
					return DANGER;
			}
			else if (!type.isAttackCapable()) 
				continue;
			else
				return DANGER;
		}
		
		if (weakestWorker != null)
			return ATTACKING;
		return SCOUTING;
		
	}
	
	@Override
	protected void updateSquadPos() {
		squadCenter.x = scout.getX();
		squadCenter.y = scout.getY();
		JavaBot.bwapi.drawCircle(squadCenter.x, squadCenter.y, DANGER_RADIUS, BWColor.RED, false, false);
		JavaBot.bwapi.drawCircle(squadCenter.x, squadCenter.y, SCOUT_RADIUS, BWColor.GREEN, false, false);
	}
	
	public void scout() {
		if(status == SCOUTING || !scout.isMoving()) {
			for(BaseLocation base : ScoutManager.bases) {
				if (inRange(new Point(base.getX(), base.getY()), new Point(JavaBot.homePositionX, JavaBot.homePositionY), 400))
					continue;
				else if(!ScoutManager.mainFound) {
					if(base.isStartLocation()) {
						JavaBot.bwapi.move(scout.getID(), base.getX(), base.getY());
					}
				}
			}
		}
	}

	public int getUnitId() {
		return scout.getID();
	}
	
	/**
	 * compares probe against enemy miner
	 * TERRAN HP = 60
	 * DRONE HP = 40
	 * PROBE HP = 20 + 20(shield)
	 * @param enemy miner
	 * @return true if probe will not win
	 */
	private boolean isStronger(Unit enemyWorker) {
		int scoutHp = scout.getHitPoints() + scout.getShield();
		
		if (enemyWorker.getTypeID() == UnitTypes.Terran_SCV.ordinal())
			return enemyWorker.getHitPoints() > (scoutHp + 20);
		else if (enemyWorker.getTypeID() == UnitTypes.Protoss_Probe.ordinal())
			return enemyWorker.getHitPoints() + enemyWorker.getShield() > scoutHp;
		else
			return enemyWorker.getHitPoints() > scoutHp;
	}
}

