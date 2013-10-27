package javabot.controllers;

import javabot.JavaBot;
import javabot.models.Unit;
import javabot.types.UnitType.UnitTypes;

public class ResourceManager {
	
	private static ResourceManager instance = null;
	
	private ResourceManager() {
		
	}
	
	public static ResourceManager getInstance() {
		if(instance == null) {
			instance = new ResourceManager();
		}
		return instance;
	}

	public void act() {
		// mine minerals with idle worker
		// Cycle over all my units,
		for (Unit unit : JavaBot.bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitTypes.Protoss_Probe.ordinal()) {
				// and if it is idle (not doing anything),
				if (unit.isIdle()) {
					// then find the closest mineral patch (if we see any)
					int closestId = -1;
					double closestDist = 99999999;
					for (Unit neu : JavaBot.bwapi.getNeutralUnits()) {
						if (neu.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal()) {
							double distance = Math.sqrt(Math.pow(neu.getX() - unit.getX(), 2) + Math.pow(neu.getY() - unit.getY(), 2));
							if ((closestId == -1) || (distance < closestDist)) {
								closestDist = distance;
								closestId = neu.getID();
							}
						}
					}
					// and (if we found it) send this worker to gather it.
					if (closestId != -1) JavaBot.bwapi.rightClick(unit.getID(), closestId);
				}
			}
		}
	}
}
