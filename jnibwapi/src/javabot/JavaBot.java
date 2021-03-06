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
import javabot.types.UnitType.UnitTypes;
import javabot.util.BWColor;
import javabot.Strategy;

public class JavaBot implements BWAPIEventListener {
	 public static JNIBWAPI bwapi;
	 public static Player player;
	 public static Unit homeBase = null;
	 public static int homePositionX;
	 public static int homePositionY;
	
	private static HashMap<String, Manager> managers = new HashMap<String, Manager>();
	
	private static Set<Integer> buildingRequests = new HashSet<Integer>();
	private static Set<Integer> armyRequests = new HashSet<Integer>();
	private static Set<Integer> workerRequests = new HashSet<Integer>();
	
	public static Deque<BuildTime> initialPriorityList = new ArrayDeque<BuildTime>();
	public static List<UnitTypes> unitPriorityList = new LinkedList<UnitTypes>();
	public static Deque<UnitTypes> buildingPriorityList = new ArrayDeque<UnitTypes>();
	public static Deque<UpgradeBuild> upgradePriorityList = new ArrayDeque<UpgradeBuild>();

	public static ArrayList<Unit> enemyBuildings = new ArrayList<Unit>();
	
	private static List<Unit> assignedUnits = new ArrayList<Unit>();
	
	private static boolean alreadyGaveScout = false;
	
	public static Strategy strat = new Strategy();
	
	private static int stopProbeNum = 0;
	private static int startProbeNum = 0;
		
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
	private void reset() {
		player = bwapi.getSelf();
		managers = new HashMap<String, Manager>();
		buildingRequests = new HashSet<Integer>();
		armyRequests = new HashSet<Integer>();
		workerRequests = new HashSet<Integer>();
		buildingPriorityList = new ArrayDeque<UnitTypes>();
		unitPriorityList = new LinkedList<UnitTypes>();
		initialPriorityList = new ArrayDeque<BuildTime>();
		assignedUnits = new ArrayList<Unit>();
		alreadyGaveScout = false;
		
		//Choose Strategy for the game
		strat.loadStrategy(bwapi.getEnemies().get(0).getRaceID());
		
		initialPriorityList = strat.getInitialList();
		unitPriorityList = strat.getUnitList();
		buildingPriorityList =strat.getBuildingList();
		upgradePriorityList = strat.getUpgradeList();
	}
	
	// Method called at the beginning of the game.
	public void gameStarted() {
		reset();
		System.out.println("Game Started");

		// allow me to manually control units during the game
		bwapi.enableUserInput();
		
		// set game speed to 30 (0 is the fastest. Tournament speed is 20)
		// You can also change the game speed from within the game by "/speed X" command.
		bwapi.setGameSpeed(20);
		
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
		//managers.put(TrashManager.class.getSimpleName(), TrashManager.getInstance());
		managers.put(UnitManager.class.getSimpleName(), UnitManager.getInstance());
		for (Manager manager : managers.values())
			manager.reset();
	}
	
	
	// Method called once every second.
	public void act() {
		for (Manager manager : managers.values())
			manager.act();
		
		//Still in the static build order time of the game
		if (!initialPriorityList.isEmpty()) {
			//In the supply time of 'okay' to build probes
			BuildTime bt = initialPriorityList.peek();
			int[] resourceCost = new int[] {0, 0};
			
			if (bt.getUnit() != null) {
				UnitType type = bwapi.getUnitType(bt.getUnit().ordinal());
				if (type == null) {
				}
				resourceCost[0] = type.getMineralPrice();
				resourceCost[1] = type.getGasPrice();
			}
			else {
				UpgradeType type = bwapi.getUpgradeType(bt.getUpgrade().getUpgrade().ordinal());
				if (type == null) {
					return;
				}
				resourceCost[0] = type.getMineralPriceBase();
				resourceCost[1] = type.getGasPriceBase();	
			}
			
			//Check if it's time to build this unit yet
			if (getSupplyCount() >= bt.getSupplyNum()) {
				if (player.getMinerals() >= resourceCost[0] && player.getMinerals() >= resourceCost[1]) {
					if (bt.getUnit() != null) {
						if (bwapi.getUnitType(bt.getUnit().ordinal()).isBuilding()) {
							BuildManager.getInstance().toBuild(bt.getUnit());
						}
						else {
							BuildManager.getInstance().toTrain(bt.getUnit());
						}
					}
					else {
						BuildManager.getInstance().toUpgrade(bt.getUpgrade());
					}
					initialPriorityList.pop();
				}
			}
			//Not in the 'dead' zone of strat of not building probes, and not waiting for minerals for next unit build
			//TODO i recommend that probes be placed into strategy and not be independently checked
			if ((ResourceManager.getInstance().getProbeCount() < stopProbeNum || ResourceManager.getInstance().getProbeCount() >= startProbeNum) && BuildManager.getInstance().canTrain(UnitTypes.Protoss_Probe)) {
				BuildManager.getInstance().toTrain(UnitTypes.Protoss_Probe);
			}
			/*
			if ((!(player.getSupplyTotal() / 2 >= stopProbeNum && player.getSupplyTotal() / 2 <= startProbeNum)) && player.getSupplyTotal() / 2 != bt.getSupplyNum()) {
				if (player.getMinerals() >= bwapi.getUnitType(UnitTypes.Protoss_Probe.ordinal()).getMineralPrice()) {
					BuildManager.getInstance().toTrain(UnitTypes.Protoss_Probe);
				}
			}
			*/
		}
		//No longer in static build order. Based off of unitPriorityList and buildingPriorityList, call for stuff to be built
		else {
			//Build a pylon when we need it
			if (getSupplyAvailable() < 4 || (getSupplyAvailable() < 6 && player.getSupplyTotal()/2 > 60)) {
				BuildManager.getInstance().toBuild(UnitTypes.Protoss_Pylon);
			}
			if(buildingPriorityList.peek() != null) {
				UnitType type = bwapi.getUnitType(buildingPriorityList.peek().ordinal());
				if (player.getMinerals() >= type.getMineralPrice() && player.getGas() >= type.getGasPrice()) {
					BuildManager.getInstance().toBuild(buildingPriorityList.peek());
				}
			}
			if (upgradePriorityList.peek() != null) {
				BuildManager.getInstance().toUpgrade(upgradePriorityList.pop());
			}
			//Unreliable count of minerals - worker currently moving to build something but hasn't got there yet
			/*
			else if (BuildManager.getInstance().workerMovingToBuild) {
			}*/
			//Don't have the minerals to try to build something. Lets check if we can build a unit we need instead
			//for (int i = 0; i < unitPriorityList.size(); i++) {
				UnitType type = bwapi.getUnitType(unitPriorityList.get(0).ordinal());
				if (player.getMinerals() >= type.getMineralPrice() && player.getGas() >= type.getGasPrice() && type.getSupplyRequired() /*/ 2*/ <= getSupplyAvailable()) {
					//Checks to see if there are any buildings that can build that unit that aren't currently building anything
					if (BuildManager.getInstance().canTrain(unitPriorityList.get(0))) {
						BuildManager.getInstance().toTrain(unitPriorityList.get(0));
						unitPriorityList.add(unitPriorityList.remove(0));
						
					}
				}
			//}	
		}
		
		//build intercepters (the little guys on carriers)
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitTypes.Protoss_Carrier.ordinal()) {
				bwapi.train(unit.getID(), UnitTypes.Protoss_Interceptor.ordinal());
			}
		}
	}
	
	
	// Method called on every frame (approximately 30x every second).
	public void gameUpdate() {
		
		// Remember our homeTilePosition at the first frame
		if (bwapi.getFrameCount() == 1) {
			int cc = BuildManager.getInstance().getNearestUnit(UnitTypes.Protoss_Nexus.ordinal(), 0, 0);
			homeBase = bwapi.getUnit(cc);
			homePositionX = homeBase.getX();
			homePositionY = homeBase.getY();
			
			BuildManager.getInstance().assignUnit(bwapi.getUnit(cc));
			ResourceManager.getInstance().gameStart(bwapi.getMyUnits());
		}
		
		/*
		if(ResourceManager.getInstance().numWorkers() == 10 && ScoutManager.getInstance().numScouts() == 0) {
			bwapi.printText("Assigning a scout");
			needsScout();
		}
		*/
		
		for (Manager manager : managers.values())
			manager.gameUpdate();
		
		// Draw debug information on screen
		drawDebugInfo();
		
		/*if (bwapi.getFrameCount() % 3500 == 0) {
			scoutAssignUnit(ResourceManager.getInstance().giveMineralUnit(), ScoutManager.class.getSimpleName());
		}*/
		
		// Call the act() method every 30 frames
		if (bwapi.getFrameCount() % 30 == 0) {
			act();
		}
		
	}
	
	/**
	 * Event fired (from BuildManager) to notify Javabot that a building has been completed 
	 * @param unitID building
	 */
	public static void buildingComplete(int unitId) {
		Unit unit = bwapi.getUnit(unitId);
		int type = unit.getTypeID();
		
		if (type == UnitTypes.Protoss_Assimilator.ordinal()) {
			ResourceManager.getInstance().assignUnit(unit);
		}
			
	}
	
	
	/**
	 * Event fired when unit/building has started construction 
	 * Assigns probes to ResourceManager
	 * Assigns combat units to ArmyManager
	 * Assigns buildings to BuildingManager
	 * @param unitID id of unit created
	 */
	public void unitCreate(int unitID) {
		Unit u = bwapi.getUnit(unitID);
		UnitType type = bwapi.getUnitType(u.getTypeID());
		
		if (type.isWorker()) {
			assignUnit(bwapi.getUnit(unitID), BuildManager.class.getSimpleName());
		}
		else if (type.isAttackCapable() || type.isSpellcaster()) {
			assignUnit(bwapi.getUnit(unitID), BuildManager.class.getSimpleName());
		}
		else if (type.isBuilding()) {
			int builderId = bwapi.getUnit(BuildManager.getInstance().getNearestUnit(UnitTypes.Protoss_Probe.ordinal(), u.getX(), u.getY())).getID();
			
			assignUnit(bwapi.getUnit(unitID), BuildManager.class.getSimpleName());
			
			//reassigns worker from resource mgr -> scout mgr on start of first pylon construction
			if (u.getTypeID() == UnitTypes.Protoss_Pylon.ordinal() && BuildManager.getInstance().getBuildingCount(UnitTypes.Protoss_Pylon.ordinal()) == 1) {
				if (!alreadyGaveScout) {
					alreadyGaveScout = true;
					assignUnit(bwapi.getUnit(ResourceManager.getInstance().removeUnit(builderId)), ScoutManager.class.getSimpleName());
				}
			}
		}
	}
	
	public static void unitCreated(int unitID) {
		Unit u = bwapi.getUnit(unitID);
		UnitType type = bwapi.getUnitType(u.getTypeID());
		
		if (type.isWorker()) {
			assignUnit(bwapi.getUnit(unitID), ResourceManager.class.getSimpleName());
		}
		else if (type.isAttackCapable() || type.isSpellcaster()) {
			assignUnit(bwapi.getUnit(unitID), ArmyManager.class.getSimpleName());
		}
	}
	
	
	public static int getSupplyCount() {
		return player.getSupplyUsed()/2;
	}
	
	public static int getSupplyAvailable() {
		return player.getSupplyTotal()/2 - player.getSupplyUsed()/2;
	}
	
	/**
	 * Assigns unit to specific manager as well as internal JavaBot table
	 * @param unit unit to add
	 * @param manager manager to assign to
	 */
	private static void assignUnit(Unit unit, String manager) {
		assignedUnits.add(unit);
		managers.get(manager).assignUnit(unit);
	}
	
	/*private static void scoutAssignUnit(Unit myUnit, String toManager) {
		JavaBot.bwapi.printText("Removed mineral worker for scout");

		if (myUnit.getTypeID() == UnitTypes.Protoss_Probe.ordinal())
			managers.get(toManager).assignUnit(myUnit);
	}*/
	
	/**
	 * Reassings uni
	 * @param unitId
	 * @param fromManager
	 */
	public static void reassignUnit(int unitId, String fromManager) {
		Unit unit = bwapi.getUnit(managers.get(fromManager).removeUnit(unitId));
		
		if (unit.getTypeID() == UnitTypes.Protoss_Probe.ordinal())
			assignUnit(unit, ResourceManager.class.getSimpleName());
		
	}
	
	// Draws debug information on the screen. 
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
        	if (ScoutManager.getInstance().numScouts() == 0 || ScoutManager.getInstance().getFirstSquad().scout == null)
        		reassignUnit(ResourceManager.getInstance().giveMineralUnit().getID(), ScoutManager.getInstance().toString());
        	else {
        		workerRequests.add(unit);
        	}
        }
        else {
        	armyRequests.add(unit);
        }
	}
	
	public void unitDestroy(int unitID) {
		int removed = -1;
		
		for (Manager manager : managers.values()) {
			removed = manager.removeUnit(unitID);
			
			UnitType type = bwapi.getUnitType(unitID);

			if(type.getRaceID() == 2 && removed != -1) {
				UnitTypes replacement = strat.TypetoTypes(type.getID());

				//System.out.println(type.getName() + " destroyed");
			
				if(type.isBuilding()) {
					buildingPriorityList.add(replacement);
					//System.out.println(buildingPriorityList.toString());
				}
				else {
					unitPriorityList.add(replacement);
					//System.out.println(unitPriorityList.toString());
				}
			}
		}
	}
	
	/**
	 * Called when unit changes type.
	 * For examplem, vespene geyser changes to Protoss Assimilator
	 */
	public void unitMorph(int unitID) {
		Unit unit = bwapi.getUnit(unitID);
		if (unit.getTypeID() == UnitTypes.Protoss_Assimilator.ordinal() && unit.isBeingConstructed())
			BuildManager.getInstance().assignUnit(unit);
	}
	
	public static void urgentInitial(BuildTime init) {
		initialPriorityList.addFirst(init);
	}
	
	public static void urgentUnit(UnitTypes unit) {
		unitPriorityList.add(0, unit);
	}
	
	public static void urgentBuilding(UnitTypes building) {
		buildingPriorityList.addFirst(building);
	}
	
	public static void urgentUpgrade(UpgradeBuild upgrade) {
		upgradePriorityList.addFirst(upgrade);
	}

	// Some additional event-related methods.
	public void gameEnded() {}
	public void matchEnded(boolean winner) {}
	public void nukeDetect(int x, int y) {}
	public void nukeDetect() {}
	public void playerLeft(int id) {}
	
	public void unitDiscover(int unitID) {}
	public void unitEvade(int unitID) {}
	public void unitHide(int unitID) {}
	public void unitShow(int unitID) {}
	public void keyPressed(int keyCode) {}
}

