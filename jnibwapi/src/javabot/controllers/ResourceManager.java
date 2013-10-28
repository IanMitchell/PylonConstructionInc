package javabot.controllers;

import java.util.ArrayList;

import javabot.types.*;
import javabot.types.UnitType.UnitTypes;
import javabot.JavaBot;
import javabot.models.Unit;

public class ResourceManager implements Manager {
	private static ArrayList<Unit> mineralWorkers = new ArrayList<Unit>();
	private static ArrayList<Unit> gasWorkers = new ArrayList<Unit>();

	private static ArrayList<Unit> mineralNodes = new ArrayList<Unit>();
	private static ArrayList<Unit> gasNodes = new ArrayList<Unit>();

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


	public void gameStart(ArrayList<Unit> units) {
		mineralWorkers = units;
		int counter = 0;

		for (Unit neu : JavaBot.bwapi.getNeutralUnits()) {
			if (neu.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal()) {
				mineralNodes.add(neu);
			}

			if (counter < units.size()) {
				JavaBot.bwapi.rightClick(mineralWorkers.get(counter).getID(), neu.getID());
				counter++;
			}
		}
	}

	public void assignUnit(Unit u) {
		if (u.getTypeID() == UnitTypes.Protoss_Probe.ordinal()) {
			mineralWorkers.add(u);
		}
		if (u.getTypeID() == UnitTypes.Protoss_Assimilator.ordinal()) {
			gasNodes.add(u);

			if (mineralWorkers.size() > 3) {
				JavaBot.bwapi.rightClick(mineralWorkers.get(0).getID(), u.getID());
				JavaBot.bwapi.rightClick(mineralWorkers.get(1).getID(), u.getID());
				JavaBot.bwapi.rightClick(mineralWorkers.get(2).getID(), u.getID());

				gasWorkers.add(mineralWorkers.get(0));
				gasWorkers.add(mineralWorkers.get(1));
				gasWorkers.add(mineralWorkers.get(2));
				mineralWorkers.remove(0);
				mineralWorkers.remove(1);
				mineralWorkers.remove(2);
			}
		}
		else {
			JavaBot.bwapi.printText("Resource Manager assigned non-probe unit.");
			JavaBot.assignUnit(u);
		}
	}

	public int mineralUnitCount() {
		return mineralWorkers.size();
	}

	public int gasUnitCount() {
		return gasWorkers.size();
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
					JavaBot.bwapi.rightClick(unit.getID(), closestId);
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
					JavaBot.bwapi.rightClick(unit.getID(), closestId);
				}
				else {
					JavaBot.bwapi.printText("Idle Mineral Worker with nothing to do.");
					mineralWorkers.remove(unit);
					JavaBot.assignUnit(unit);
				}
			}
		}

		if (assimilatorRequested == false && mineralWorkers.size() >= 16) {
			JavaBot.requestUnit(UnitTypes.Protoss_Assimilator.ordinal());
			assimilatorRequested = true;
		}
	}

	@Override
	public void gameUpdate() {
		// TODO Auto-generated method stub
		
	}
	
	public static Unit requestScout() {
		//Gives up a worker if the initial build is ready, so it can grow up and be a scout
		//If not ready, returns null.
		return null;
	}
}
