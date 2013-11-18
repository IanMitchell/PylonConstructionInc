package javabot.controllers;

import javabot.JavaBot;
import javabot.models.Unit;
import javabot.types.UnitType.UnitTypes;

public class UnitManager implements Manager {
	private static UnitManager instance = null;
	
	private UnitTypes nextToBuild = null;
	
	private UnitManager() {
		
	}
	
	public static UnitManager getInstance() {
		if(instance == null) {
			instance = new UnitManager();
		}
		return instance;
	}
	
	public void reset() {
		//No static variables?
	}

	@Override
	public void act() {
		if (nextToBuild != null) {
			//BUILD THIS UNIT
			nextToBuild = null;
		}
		
		/*
		//train probes
		for (Unit unit : JavaBot.bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitTypes.Protoss_Nexus.ordinal()) {
				// if it's training queue is empty
				if (unit.getTrainingQueueSize() == 0) {
					// check if we have enough minerals and supply, and (if we do) train one worker (Probe)
					if ((JavaBot.bwapi.getSelf().getMinerals() >= 50) &&
				     (JavaBot.bwapi.getSelf().getSupplyTotal()-JavaBot.bwapi.getSelf().getSupplyUsed() >= 2)) 
						JavaBot.bwapi.train(unit.getID(), UnitTypes.Protoss_Probe.ordinal());
				}
			}
			else if(unit.getTypeID() == UnitTypes.Protoss_Gateway.ordinal()) {
				if(unit.getTrainingQueueSize() == 0) {
					if((JavaBot.bwapi.getSelf().getMinerals() >= 50) &&
					 (JavaBot.bwapi.getSelf().getSupplyTotal()-JavaBot.bwapi.getSelf().getSupplyUsed() >= 2)) {
						 JavaBot.bwapi.train(unit.getID(), UnitTypes.Protoss_Zealot.ordinal());
					 }
				}
			}
		}
		*/
	}

	@Override
	public void gameUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void assignUnit(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int removeUnit(int unitId) {
		// TODO Auto-generated method stub
		return -1;
	}
}