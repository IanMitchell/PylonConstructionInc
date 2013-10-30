package javabot;

import java.awt.Point;
import java.util.*;

import javabot.controllers.ArmyManager;
import javabot.controllers.BuildManager;
import javabot.controllers.Manager;
import javabot.controllers.ResourceManager;
import javabot.controllers.ScoutManager;
import javabot.controllers.TrashManager;
import javabot.controllers.UnitManager;
import javabot.models.*;
import javabot.types.*;
import javabot.types.OrderType.OrderTypeTypes;
import javabot.types.UnitType.UnitTypes;
import javabot.util.BWColor;

public class JavaBot implements BWAPIEventListener {
	public static JNIBWAPI bwapi;
	public static int homePositionX;
	public static int homePositionY;
	
	private HashMap<String, Manager> managers = new HashMap<String, Manager>();
	
	private static Set<Integer> buildingRequests = new HashSet<Integer>();
	private static Set<Integer> armyRequests = new HashSet<Integer>();
	private static Set<Integer> workerRequests = new HashSet<Integer>();
	
	private static List<Unit> buildingQueue = new ArrayList<Unit>();
	private static List<Unit> armyQueue = new ArrayList<Unit>();
	
	private static List<Unit> assignedUnits = new ArrayList<Unit>();
	
	public static enum Priority {WORKERS, BUILDINGS, ARMY};
		
	private static Priority hasPriority = Priority.ARMY;
	
	public static void main(String[] args) {
		new JavaBot();
	}
	public JavaBot() {
		bwapi = new JNIBWAPI(this);
		bwapi.start();
	} 
	public void connected() {
		bwapi.loadTypeData();
	}
	
	// Method called at the beginning of the game.
	public void gameStarted() {		
		System.out.println("Game Started");

		// allow me to manually control units during the game
		bwapi.enableUserInput();
		
		// set game speed to 30 (0 is the fastest. Tournament speed is 20)
		// You can also change the game speed from within the game by "/speed X" command.
		bwapi.setGameSpeed(25);
		
		// analyze the map
		bwapi.loadMapData(true);
		

		// This is called at the beginning of the game. You can 
		// initialize some data structures (or do something similar) 
		// if needed. For example, you should maintain a memory of seen 
		// enemy buildings.
		bwapi.printText("This map is called "+bwapi.getMap().getName());
		bwapi.printText("Enemy race ID: "+String.valueOf(bwapi.getEnemies().get(0).getRaceID()));	// Z=0,T=1,P=2
		
		managers.put(ArmyManager.class.getSimpleName(), ArmyManager.getInstance());
		managers.put(BuildManager.class.getSimpleName(), BuildManager.getInstance());
		managers.put(ResourceManager.class.getSimpleName(), ResourceManager.getInstance());
		managers.put(ScoutManager.class.getSimpleName(), ScoutManager.getInstance());
		managers.put(TrashManager.class.getSimpleName(), TrashManager.getInstance());
		managers.put(UnitManager.class.getSimpleName(), UnitManager.getInstance());
	}
	
	
	// Method called once every second.
	public void act() {
		for (Manager manager : managers.values())
			manager.act();
		if (hasPriority == Priority.ARMY && armyQueue.size() > 0) {
			//Tell ArmyManager to build top unit in queue
		}
		else if (hasPriority == Priority.BUILDINGS && buildingQueue.size() > 0) {
			//Tell BuildManager to build top building in queue
		}
		else if (hasPriority == Priority.WORKERS) {
			//ResourceManager has requested more workers
		}
	}
	
	
	// Method called on every frame (approximately 30x every second).
	public void gameUpdate() {
		
		// Remember our homeTilePosition at the first frame
		if (bwapi.getFrameCount() == 1) {
			int cc = BuildManager.getNearestUnit(UnitTypes.Terran_Command_Center.ordinal(), 0, 0);
			if (cc == -1) cc = BuildManager.getNearestUnit(UnitTypes.Zerg_Hatchery.ordinal(), 0, 0);
			if (cc == -1) cc = BuildManager.getNearestUnit(UnitTypes.Protoss_Nexus.ordinal(), 0, 0);
			homePositionX = bwapi.getUnit(cc).getX();
			homePositionY = bwapi.getUnit(cc).getY();
		
			((ResourceManager) managers.get(ResourceManager.class.getSimpleName())).gameStart(bwapi.getMyUnits());
		}
		
		if(ResourceManager.getInstance().numWorkers() == 10 && ScoutManager.getInstance().numScouts() == 0) {
			bwapi.printText("Assigning a scout");
			needsScout();
		}
		
		for (Manager manager : managers.values())
			manager.gameUpdate();
		
		// Draw debug information on screen
		drawDebugInfo();

		
		// Call the act() method every 30 frames
		if (bwapi.getFrameCount() % 30 == 0) {
			act();
		}
	}
	
	/**
	 * Event fired when unit is created.
	 * Assigns probes to ResourceManager
	 * Assigns combat units to ArmyManager
	 * Assigns buildings to ...?
	 * @param unitID id of unit created
	 */
	public void unitCreate(int unitID) {
		Unit u = bwapi.getUnit(unitID);
		UnitType type = bwapi.getUnitType(u.getTypeID());
		bwapi.printText(type.getName() + " has been created.");
		
		if (type.isWorker()) {
			bwapi.printText("Assigning worker to ResourceManager");
			assignUnit(bwapi.getUnit(unitID), "ResourceManager");
		}
		else if (type.isAttackCapable() || type.isSpellcaster()) {
			bwapi.printText("Assigning attacking unit to ArmyManager");
			assignUnit(bwapi.getUnit(unitID), ArmyManager.class.getSimpleName());
		}
		else if (type.isBuilding()) {
			bwapi.printText("Assigning building to...?");
			// TODO 
		}
	}
	
	/**
	 * Assigns unit to specific manager as well as internal JavaBot table
	 * @param unit unit to add
	 * @param manager manager to assign to
	 */
	public void assignUnit(Unit unit, String manager) {
		assignedUnits.add(unit);
		managers.get(manager).assignUnit(unit);
	}
	
	// Draws debug information on the screen. 
	// Reimplement this function however you want. 
	public void drawDebugInfo() {

		// Draw our home position.
		bwapi.drawText(new Point(5,0), "Our home position: "+String.valueOf(homePositionX)+","+String.valueOf(homePositionY), true);
		
		// Draw circles over workers (blue if they're gathering minerals, green if gas, white if inactive)
		for (Unit u : bwapi.getMyUnits())  {
			if (u.isGatheringMinerals()) 
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.BLUE, false, false);
			else if (u.isGatheringGas())
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.GREEN, false, false);
			else if (u.getTypeID() == UnitTypes.Protoss_Probe.ordinal() && u.isIdle())
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.WHITE, false, false);
				
		}
		
	}
	
	public static void requestUnit(int unit) {
        UnitType type = bwapi.getUnitType(unit);
        if (type.isBuilding())
        	buildingRequests.add(unit);
        else if (type.isWorker()){
        	workerRequests.add(unit);
        }
        else {
        	armyRequests.add(unit);
        }
	}
	
	public static void needsScout() {
		Unit scout = ResourceManager.requestWorker();
		ScoutManager.getInstance().assignUnit(scout);
	}

	// Some additional event-related methods.
	public void gameEnded() {}
	public void matchEnded(boolean winner) {}
	public void nukeDetect(int x, int y) {}
	public void nukeDetect() {}
	public void playerLeft(int id) {}
	public void unitDestroy(int unitID) {}
	public void unitDiscover(int unitID) {}
	public void unitEvade(int unitID) {}
	public void unitHide(int unitID) {}
	public void unitMorph(int unitID) {}
	public void unitShow(int unitID) {}
	public void keyPressed(int keyCode) {}
}
