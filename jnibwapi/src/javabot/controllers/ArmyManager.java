package javabot.controllers;

import java.awt.Point;
import java.util.*;

import javabot.models.*;

public class ArmyManager implements Manager {
	private static ArmyManager instance = null;
	private ArrayList<Squad> squads;
	private Point enemyMain;
	
	private ArmyManager() {
		squads = new ArrayList<Squad>();
		squads.add(new Squad());
	}
	public void reset() {
		squads = new ArrayList<Squad>();
		enemyMain = null;
	}
	
	public static ArmyManager getInstance() {
		if(instance == null) {
			instance = new ArmyManager();
		}
		return instance;
	}
	
	@Override
	public void assignUnit(Unit unit) {
		if(squads.size() == 0) {
			newSquad();
		}
		squads.get(0).assignUnit(unit);
	}


	public void act() {
		
	}
	
	private void newSquad() {
		Squad squad = new Squad();
		squad.setRallyPoint(enemyMain);
		squads.add(squad);
	}

	public void gameUpdate() {
		for(Squad squad : squads) {
			squad.update();
		}
	}
	
	public void setEnemyMain(int x, int y) {
		enemyMain = new Point(x,y);
		for(Squad squad : squads) {
			squad.setRallyPoint(enemyMain);
		}
	}

	@Override
	public int removeUnit(int unitId) {
		int id = -1;
		
		for(Squad squad : squads)
			id = Math.max(squad.removeUnit(unitId), -1);
		
		return id;
	}
}
