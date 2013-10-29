package javabot.controllers;

import java.util.*;

import javabot.JavaBot;
import javabot.models.*;
import javabot.types.UnitType.UnitTypes;

public class ArmyManager implements Manager {
	private static ArmyManager instance = null;
	private ArrayList<Squad> squads;
	private HashSet<Unit> assigned;
	
	private ArmyManager() {
		squads = new ArrayList<Squad>();
		squads.add(new Squad());
		assigned = new HashSet<Unit>();
	}
	
	public static ArmyManager getInstance() {
		if(instance == null) {
			instance = new ArmyManager();
		}
		return instance;
	}
	
	@Override
	public void assignUnit(Unit unit) {
		Squad squad = squads.get(0);
		squad.assignUnit(unit);
	}


	public void act() {
		
	}

	public void gameUpdate() {
		/*for(Unit unit : JavaBot.bwapi.getMyUnits()) {
			if(unit.getTypeID() == UnitTypes.Protoss_Zealot.ordinal()) {
				if(!assigned.contains(unit)) {
					assignUnit(unit);
					assigned.add(unit);
				}
			}
		}*/
		for(Squad squad : squads) {
			squad.update();
		}
	}
}
