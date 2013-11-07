package javabot.controllers;

import java.util.ArrayList;

import javabot.JavaBot;
import javabot.models.BaseLocation;
import javabot.models.ScoutSquad;
import javabot.models.Unit;
import javabot.types.UnitType.UnitTypes;


public class ScoutManager implements Manager {
	private static ScoutManager instance = null;
	private ArrayList<ScoutSquad> scoutSquads;
	public static ArrayList<BaseLocation> bases;
	public static boolean mainFound;
	
	//-1 means no scout ever assigned. 0 means scout dead. 1 means scout is alive and kickin
	private static int status = -1;
	
	private ScoutManager() {
		scoutSquads = new ArrayList<ScoutSquad>();
		bases = JavaBot.bwapi.getMap().getBaseLocations();
		mainFound = false;
	}
	
	public void reset() {
		scoutSquads = new ArrayList<ScoutSquad>();
		bases = JavaBot.bwapi.getMap().getBaseLocations();
		mainFound = false;
	}
	
	public static ScoutManager getInstance() {
		if(instance == null) {
			instance = new ScoutManager();
		}
		return instance;
	}
	
	public void act() {
		if (status == 0) {
			JavaBot.requestUnit(UnitTypes.Protoss_Probe.ordinal());
		}
	}
	

	public void gameUpdate() {
		for(ScoutSquad scoutSquad : scoutSquads) {
			scoutSquad.update();
		}
	}
	
	@Override
	public void assignUnit(Unit unit) {
		scoutSquads.add(new ScoutSquad(unit));
		status = 1;
	}
	
	public int numScouts() {
		return scoutSquads.size();
	}

	@Override
	public int removeUnit(int unitId) {
		for(int i=0; i<scoutSquads.size(); i++) {
			ScoutSquad scout = scoutSquads.get(i);
			if (unitId == scout .getUnitId()) {
				status = 0;
				return scoutSquads.remove(i).getUnitId();
			}
		}
		return -1;
	}
}
