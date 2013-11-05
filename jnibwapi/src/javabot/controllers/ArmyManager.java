package javabot.controllers;

import java.util.*;

import javabot.JavaBot;
import javabot.models.*;
import javabot.types.UnitType.UnitTypes;

public class ArmyManager implements Manager {
	private static ArmyManager instance = null;
	private ArrayList<Squad> squads;
	private int enemyMainX = 0;
	private int enemyMainY = 0;
	
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
		squad.setRallyPoint(enemyMainX, enemyMainY);
		squads.add(squad);
	}

	public void gameUpdate() {
		for(Squad squad : squads) {
			squad.update();
		}
	}
	
	public void setEnemyMain(int x, int y) {
		enemyMainX = x;
		enemyMainY = y;
		for(Squad squad : squads) {
			squad.setRallyPoint(enemyMainX, enemyMainY);
		}
	}

	@Override
	public int removeUnit(int unitId) {
		// TODO Auto-generated method stub
		return -1;
	}
}
