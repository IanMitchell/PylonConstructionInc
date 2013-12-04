package javabot.controllers;

import java.util.ArrayList;

import javabot.types.UnitType.UnitTypes;
import javabot.JavaBot;
import javabot.models.Unit;

public class ResourceManager implements Manager {
	private static ArrayList<Unit> mineralWorkers = new ArrayList<Unit>();
	private static ArrayList<Unit> gasWorkers = new ArrayList<Unit>();

	private static ArrayList<Unit> mineralNodes = new ArrayList<Unit>();
	private static ArrayList<Unit> gasNodes = new ArrayList<Unit>();

	private static int unitsNeeded = 0;
	
	private static Boolean assimilatorRequested = false;

	private static ResourceManager instance = null;

	
	private ResourceManager() {

	}

	public static ResourceManager getInstance() {
		if(instance == null) {
			instance = new ResourceManager();
		}
		return instance;
	}
	
	public void reset() {
		mineralWorkers = new ArrayList<Unit>();
		gasWorkers = new ArrayList<Unit>();
		mineralNodes = new ArrayList<Unit>();
		gasNodes = new ArrayList<Unit>();
		assimilatorRequested = false;
		instance = null;
	}

	public void gameStart(ArrayList<Unit> units) {
		int counter = 0;
		
		for (Unit unit : units) {
			if (unit.getTypeID() == UnitTypes.Protoss_Probe.ordinal()) {
				mineralWorkers.add(unit);
			}
		}
		
		// We don't want all units to go to the same node,
		// so each starting probe gets sent to a new spot.
		// The probes only seek open nodes when returning from the base,
		// so the initial units all wait until the node is free initially,
		// delaying our start significantly.
		for (Unit neu : JavaBot.bwapi.getNeutralUnits()) {
			if (neu.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal()) {
				mineralNodes.add(neu);
				unitsNeeded += 2;
			
				if (counter < units.size()) {
					JavaBot.bwapi.gather(mineralWorkers.get(counter).getID(), neu.getID());
					counter++;
				}
			}
		}
	}

	public void newBase(Unit base) {
		double maxDistance = 400;

		for (Unit n : JavaBot.bwapi.getNeutralUnits()) {
			if (n.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal()) {
				double distance = Math.sqrt(Math.pow(n.getX() - base.getX(), 2) + Math.pow(n.getY() - base.getY(), 2));
				if (distance <= maxDistance) {
					mineralNodes.add(n);
					unitsNeeded += 2;
				}
			}
		}
	}
	
	private int getClosestMineralWorker(Unit unit) {
		int closestId = -1;
		double closestDist = 99999999;

		for (Unit n : mineralWorkers) {
			double distance = Math.sqrt(Math.pow(n.getX() - unit.getX(), 2) + Math.pow(n.getY() - unit.getY(), 2));
			if ((closestId == -1) || (distance < closestDist)) {
				closestDist = distance;
				closestId = n.getID();
			}
		}
		
		if (closestId != -1) {
			return closestId;
		}
		
		return 0;
	}

	@Override
	public void assignUnit(Unit u) {
		if (u.getTypeID() == UnitTypes.Protoss_Probe.ordinal() && !mineralWorkers.contains(u)) {
			mineralWorkers.add(u);
		}
		else if (u.getTypeID() == UnitTypes.Protoss_Assimilator.ordinal() && !gasNodes.contains(u)) {
			gasNodes.add(u);
						
			for (int i = 0; i < 3; i++) {
				if (mineralWorkers.size() > 0) {
					int id = getClosestMineralWorker(u);
					try {
						for (Unit worker : mineralWorkers) {
							if (worker.getID() == id) {
								JavaBot.bwapi.rightClick(id, u.getID());
								gasWorkers.add(worker);
								mineralWorkers.remove(worker);
							}
						}

					} catch (Throwable t) {
						System.out.println(t.getMessage());
					}

				}
			}
			
			unitsNeeded += 3;
		}
		else {
			JavaBot.bwapi.printText("Resource Manager assigned non-probe unit.");
		}
	}

	public int mineralUnitCount() {
		return mineralWorkers.size();
	}

	public int gasUnitCount() {
		return gasWorkers.size();
	}
	
	public int getProbeCount() {
		return mineralWorkers.size() + gasWorkers.size();
	}

	@Override
	public void act() {
		for (Unit unit : gasWorkers) {
			if (unit.isIdle()) {
				int closestId = -1;
				double closestDist = 99999999;

				for (Unit neu : gasNodes) {
					double distance = Math.sqrt(Math.pow(neu.getX() - unit.getX(), 2) + Math.pow(neu.getY() - unit.getY(), 2));
					if ((closestId == -1) || (distance < closestDist)) {
						closestDist = distance;
						closestId = neu.getID();
					}
				}
				// and (if we found it) send this worker to gather it.
				if (closestId != -1) {
					JavaBot.bwapi.gather(unit.getID(), closestId);
				}
				else {
					JavaBot.bwapi.printText("Idle Gas Worker with nothing to do.");
					gasWorkers.remove(unit);
					mineralWorkers.add(unit);
				}
			}
		}

		for (Unit unit : mineralWorkers) {
			if (unit.isIdle()) {
				int closestId = -1;
				double closestDist = 99999999;
				
				for (Unit neu : mineralNodes) {
					double distance = Math.sqrt(Math.pow(neu.getX() - unit.getX(), 2) + Math.pow(neu.getY() - unit.getY(), 2));
					if ((closestId == -1) || (distance < closestDist)) {
						closestDist = distance;
						closestId = neu.getID();
					}
				}
				// and (if we found it) send this worker to gather it.
				if (closestId != -1) {
					JavaBot.bwapi.gather(unit.getID(), closestId);
				}
				else {
					JavaBot.bwapi.printText("Idle Mineral Worker with nothing to do.");
				}
			}
		}
	}

	@Override
	public void gameUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int removeUnit(int unitId) {
		int maxUnits = Math.max(mineralWorkers.size(), gasWorkers.size());
		for (int i=0; i < maxUnits; i++) {
			if (i < mineralWorkers.size() && mineralWorkers.get(i).getID() == unitId) {
				JavaBot.bwapi.printText("Removed mineral worker");
				return mineralWorkers.remove(i).getID();
			}
			else if (i < gasWorkers.size() && gasWorkers.get(i).getID() == unitId) {
				JavaBot.bwapi.printText("Removed gas worker");
				return gasWorkers.remove(i).getID();
			}
		}
				
		return -1;
	}
	
	
	public Unit giveMineralUnit() {
		return mineralWorkers.remove(0);
	}
	
	public ArrayList<Unit> getGasNodes() {
		return gasNodes;
	}
	
	public Boolean needsProbes() {
		return unitsNeeded < gasWorkers.size() + mineralWorkers.size();
	}
	
}
