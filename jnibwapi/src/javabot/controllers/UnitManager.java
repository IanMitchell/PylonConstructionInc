package javabot.controllers;

import javabot.JavaBot;
import javabot.models.Unit;
import javabot.types.UnitType.UnitTypes;

public class UnitManager {
	private static UnitManager instance = null;
	
	private UnitManager() {
		
	}
	
	public static UnitManager getInstance() {
		if(instance == null) {
			instance = new UnitManager();
		}
		return instance;
	}

	public static void act() {
		//train probes
		for (Unit unit : JavaBot.bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitTypes.Protoss_Nexus.ordinal()) {
				// if it's training queue is empty
				if (unit.getTrainingQueueSize() == 0) {
					// check if we have enough minerals and supply, and (if we do) train one worker (Terran_SCV)
					if ((JavaBot.bwapi.getSelf().getMinerals() >= 50) &&
				     (JavaBot.bwapi.getSelf().getSupplyTotal()-JavaBot.bwapi.getSelf().getSupplyUsed() >= 2)) 
						JavaBot.bwapi.train(unit.getID(), UnitTypes.Protoss_Probe.ordinal());
				}
			}
		}
	}
}
