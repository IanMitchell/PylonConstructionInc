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
import javabot.types.TechType.TechTypes;
import javabot.types.UnitType.UnitTypes;
import javabot.util.BWColor;

public class JavaBot implements BWAPIEventListener {
	public static JNIBWAPI bwapi;
	public static int homePositionX;
	public static int homePositionY;
	
	private static HashMap<String, Manager> managers = new HashMap<String, Manager>();
	
	private static Set<Integer> buildingRequests = new HashSet<Integer>();
	private static Set<Integer> armyRequests = new HashSet<Integer>();
	private static Set<Integer> workerRequests = new HashSet<Integer>();
	
	private static Deque<BuildTime> initialPriorityList = new ArrayDeque<BuildTime>();
	public static List<UnitTypes> unitPriorityList = new LinkedList<UnitTypes>();
	public static Deque<UnitTypes> buildingPriorityList = new ArrayDeque<UnitTypes>();
	
	private static List<Unit> assignedUnits = new ArrayList<Unit>();
	
	private static boolean alreadyGaveScout = false;
	
	private static ArrayList<String> possibleStrats = new ArrayList<String>();
	
	private static String currentStrat = null;
	
	private static int stopProbeNum = 0;
	private static int startProbeNum = 0;
	
	private class BuildTime {
		private int supplyNum = 0;
		private UnitTypes unit = null;
		private TechTypes tech = null;
		public BuildTime(int supply, UnitTypes u) {
			supplyNum = supply;
			unit = u;
			tech = null;
		}
		public BuildTime(int supply, TechTypes t) {
			supplyNum = supply;
			unit = null;
			tech = t;
		}
		public int getSupplyNum() {return supplyNum;}
		public UnitTypes getUnit() {return unit;}
		public TechTypes getTech() {return tech;}
	}
	
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
		possibleStrats = new ArrayList<String>();
		possibleStrats.add("Goon Rush");
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
		Random rand = new Random();
		currentStrat = possibleStrats.get(rand.nextInt() % possibleStrats.size());
		if (currentStrat == "Goon Rush") {
			strategyGoonRush();
		}
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
		managers.put(TrashManager.class.getSimpleName(), TrashManager.getInstance());
		managers.put(UnitManager.class.getSimpleName(), UnitManager.getInstance());
		for (Manager manager : managers.values())
			manager.reset();
	}
	
	
	// Method called once every second.
	public void act() {
		for (Manager manager : managers.values())
			manager.act();
		//Still in the static build order time of the game
		if (initialPriorityList.size() > 0) {
			//In the supply time of 'okay' to build probes
			BuildTime bt = initialPriorityList.peek();
			int[] resourceCost = new int[] {0, 0};
			if (bt.getUnit() != null) {
				UnitType type = bwapi.getUnitType(bt.getUnit().ordinal());
				if (type == null) {
					bwapi.printText("Found unknown unit");
				}
				resourceCost[0] = type.getMineralPrice();
				resourceCost[1] = type.getGasPrice();
			}
			else {
				TechType type = bwapi.getTechType(bt.getTech().ordinal());
				if (type == null) {
					return;
					//bwapi.printText("Found unknown tech to research"); NEED TO FIGURE OUT HOW TO RESEARCH DRAGOON RANGE!!!!
				}
				resourceCost[0] = type.getMineralPrice();
				resourceCost[1] = type.getGasPrice();	
			}
			//Check if it's time to build this unit yet
			if (bwapi.getSelf().getSupplyTotal() / 2 >= bt.getSupplyNum()) {
				if (bwapi.getSelf().getMinerals() >= resourceCost[0] && bwapi.getSelf().getMinerals() >= resourceCost[1]) {
					if (bt.getUnit() != null) {
						if (bwapi.getUnitType(bt.getUnit().ordinal()).isBuilding()) {
							BuildManager.getInstance().toBuild(bt.getUnit());
						}
						else {
							UnitManager.getInstance().toBuild(bt.getUnit());
						}
					}
					else {
						BuildManager.getInstance().toTech(bt.getTech());
					}
					initialPriorityList.pop();
				}
			}
			//Not in the 'dead' zone of strat of not building probes, and not waiting for minerals for next unit build 
			else if ((!(bwapi.getSelf().getSupplyTotal() / 2 >= stopProbeNum && bwapi.getSelf().getSupplyTotal() / 2 <= startProbeNum)) && bwapi.getSelf().getSupplyTotal() / 2 != bt.getSupplyNum()) {
				if (bwapi.getSelf().getMinerals() >= bwapi.getUnitType(UnitTypes.Protoss_Probe.ordinal()).getMineralPrice()) {
					UnitManager.getInstance().toBuild(UnitTypes.Protoss_Probe);
				}
			}
		}
		//No longer in static build order. Based off of unitPriorityList and buildingPriorityList, call for stuff to be built
		else {
			//Build a pylon when we need it
			if ((bwapi.getSelf().getSupplyTotal() - bwapi.getSelf().getSupplyUsed()) / 2 < 4) {
				BuildManager.getInstance().toBuild(UnitTypes.Protoss_Pylon);
				return;
			}
			UnitType type = bwapi.getUnitType(buildingPriorityList.peek().ordinal());
			if (bwapi.getSelf().getMinerals() >= type.getMineralPrice() && bwapi.getSelf().getGas() >= type.getGasPrice()) {
				BuildManager.getInstance().toBuild(buildingPriorityList.pop());
				return;
			}
			//Unreliable count of minerals - worker currently moving to build something but hasn't got there yet
			else if (BuildManager.getInstance().workerMovingToBuild) {
				return;
			}
			//Don't have the minerals to try to build something. Lets check if we can build a unit we need instead
			for (int i = 0; i < unitPriorityList.size(); i++) {
				type = bwapi.getUnitType(unitPriorityList.get(i).ordinal());
				if (bwapi.getSelf().getMinerals() >= type.getMineralPrice() && bwapi.getSelf().getGas() >= type.getGasPrice()) {
					//Checks to see if there are any buildings that can build that unit that aren't currently building anything
					if (UnitManager.getInstance().canBuild(unitPriorityList.get(i))) {
						UnitManager.getInstance().toBuild(unitPriorityList.get(i));
					}
				}
			}	
		}
	}
	
	
	// Method called on every frame (approximately 30x every second).
	public void gameUpdate() {
		
		// Remember our homeTilePosition at the first frame
		if (bwapi.getFrameCount() == 1) {
			int cc = BuildManager.getInstance().getNearestUnit(UnitTypes.Terran_Command_Center.ordinal(), 0, 0);
			if (cc == -1) cc = BuildManager.getInstance().getNearestUnit(UnitTypes.Zerg_Hatchery.ordinal(), 0, 0);
			if (cc == -1) cc = BuildManager.getInstance().getNearestUnit(UnitTypes.Protoss_Nexus.ordinal(), 0, 0);
			homePositionX = bwapi.getUnit(cc).getX();
			homePositionY = bwapi.getUnit(cc).getY();
		
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
		
		// Call the act() method every 30 frames
		if (bwapi.getFrameCount() % 30 == 0) {
			act();
		}
	}
	
	/**
	 * Event fired when unit has been created or when building has started construction 
	 * Assigns probes to ResourceManager
	 * Assigns combat units to ArmyManager
	 * Assigns buildings to BuildingManager
	 * @param unitID id of unit created
	 */
	public void unitCreate(int unitID) {
		Unit u = bwapi.getUnit(unitID);
		UnitType type = bwapi.getUnitType(u.getTypeID());
		bwapi.printText(type.getName() + " has been created.");
		
		if (type.isWorker()) {
			bwapi.printText("Assigning worker to ResourceManager");
			assignUnit(bwapi.getUnit(unitID), ResourceManager.class.getSimpleName());
		}
		else if (type.isAttackCapable() || type.isSpellcaster()) {
			bwapi.printText("Assigning attacking unit to ArmyManager");
			assignUnit(bwapi.getUnit(unitID), ArmyManager.class.getSimpleName());
		}
		else if (type.isBuilding()) {
			int builderId = bwapi.getUnit(BuildManager.getInstance().getNearestUnit(UnitTypes.Protoss_Probe.ordinal(), u.getX(), u.getY())).getID();
			
			bwapi.printText("Assigning building to BuildingManager");
			assignUnit(bwapi.getUnit(unitID), BuildManager.class.getSimpleName());
			
			//reassigns worker from resource mgr -> scout mgr if first pylon built
			if (u.getTypeID() == UnitTypes.Protoss_Pylon.ordinal() && BuildManager.getInstance().getBuildingCount(UnitTypes.Protoss_Pylon.ordinal()) == 1) {
				bwapi.printText("Assigning scout to ScoutManager");
				if (!alreadyGaveScout) {
					alreadyGaveScout = true;
					bwapi.printText("Assigning scout to ScoutManager");
					assignUnit(bwapi.getUnit(ResourceManager.getInstance().removeUnit(builderId)), ScoutManager.class.getSimpleName());
				}
			}
		}
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
	
	/**
	 * Reassings unit
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
		for (Manager manager : managers.values())
			manager.removeUnit(unitID);
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
	public void unitMorph(int unitID) {}
	public void unitShow(int unitID) {}
	public void keyPressed(int keyCode) {}
	
	//ALL STRATEGIES
	private void strategyGoonRush() {
		initialPriorityList.add(new BuildTime(8, UnitTypes.Protoss_Pylon));
		initialPriorityList.add(new BuildTime(10, UnitTypes.Protoss_Gateway));
		initialPriorityList.add(new BuildTime(11, UnitTypes.Protoss_Assimilator));
		initialPriorityList.add(new BuildTime(13, UnitTypes.Protoss_Cybernetics_Core));
		initialPriorityList.add(new BuildTime(15, UnitTypes.Protoss_Gateway));
		initialPriorityList.add(new BuildTime(15, TechTypes.Undefined33)); //This 'should' be Singularity Charge, have to test in game to make sure does this correctly. Sent emails to the owners of the JNIBWAPI
		initialPriorityList.add(new BuildTime(15, UnitTypes.Protoss_Dragoon));
		initialPriorityList.add(new BuildTime(17, UnitTypes.Protoss_Pylon));
		initialPriorityList.add(new BuildTime(17, UnitTypes.Protoss_Dragoon));
		initialPriorityList.add(new BuildTime(17, UnitTypes.Protoss_Dragoon));
		initialPriorityList.add(new BuildTime(21, UnitTypes.Protoss_Pylon));
		initialPriorityList.add(new BuildTime(21, UnitTypes.Protoss_Dragoon));
		initialPriorityList.add(new BuildTime(21, UnitTypes.Protoss_Dragoon));
		initialPriorityList.add(new BuildTime(21, UnitTypes.Protoss_Dragoon));
		
		unitPriorityList.add(UnitTypes.Protoss_Probe);
		unitPriorityList.add(UnitTypes.Protoss_Dark_Templar);
		unitPriorityList.add(UnitTypes.Protoss_Dragoon);
		unitPriorityList.add(UnitTypes.Protoss_Zealot);
		
		buildingPriorityList.add(UnitTypes.Protoss_Citadel_of_Adun);
		buildingPriorityList.add(UnitTypes.Protoss_Templar_Archives);
		buildingPriorityList.add(UnitTypes.Protoss_Nexus);
		
		stopProbeNum = 15;
		startProbeNum = 29;
	}
}
