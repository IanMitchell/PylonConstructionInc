package javabot.controllers;

import java.util.ArrayList;
import javabot.JavaBot;
import javabot.models.BaseLocation;
import javabot.models.Unit;


public class ScoutManager implements Manager {
	private static ScoutManager instance = null;
	private ArrayList<Unit> scouts;
	private ArrayList<BaseLocation> bases;
	
	private ScoutManager() {
		scouts = new ArrayList<Unit>();
		bases = JavaBot.bwapi.getMap().getBaseLocations();
	}
	
	public static ScoutManager getInstance() {
		if(instance == null) {
			instance = new ScoutManager();
		}
		return instance;
	}

	@Override
	public void act() {
		for(Unit scout : scouts) {
			for(BaseLocation base : bases) {
				//bwapi.isExplored seems to be broken????
				if(JavaBot.bwapi.isExplored(base.getX(), base.getY())) {
					System.out.println("Explored this one");
				}
				else {
					System.out.println("Not explored");
					JavaBot.bwapi.move(scout.getID(), base.getX(), base.getY());
					break;
				}
			}
		}
	}
	
	//ok

	@Override
	public void gameUpdate() {
	}
	
	public void assignUnit(Unit unit) {
		scouts.add(unit);
	}
	
	public int numScouts() {
		return scouts.size();
	}
	
	public void setScout(Unit scout) {
		scouts.add(scout);
	}
}
