package javabot.controllers;

import java.util.*;

import javabot.JavaBot;
import javabot.models.*;
import javabot.types.UnitType.UnitTypes;

public class ArmyManager implements Manager {
	private static ArmyManager instance = null;
	private static ArrayList<Squad> squads;
	
	private ArmyManager() {
		squads = new ArrayList<Squad>();
		squads.add(new Squad());
	}
	
	public static ArmyManager getInstance() {
		if(instance == null) {
			instance = new ArmyManager();
		}
		return instance;
	}
	
	public static void assignUnit(Unit unit) {
		Squad squad = squads.get(0);
		squad.assignUnit(unit);
	}


	public void act() {

	}


	public void gameUpdate() {
		for(Unit unit : JavaBot.bwapi.getMyUnits()) {
			if(unit.getTypeID() == UnitTypes.Protoss_Zealot.ordinal()) {
				assignUnit(unit);
			}
		}
		for(Squad squad : squads) {
			squad.update();
		}
	}
}
