package javabot.controllers;
import javabot.models.*;

public class ArmyManager implements Manager {
	private static ArmyManager instance = null;
	
	private ArmyManager() {
		
	}
	
	public static ArmyManager getInstance() {
		if(instance == null) {
			instance = new ArmyManager();
		}
		return instance;
	}
	
	public static void assignUnit(Unit unit) {
		
	}

	@Override
	public void act() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameUpdate() {
		// TODO Auto-generated method stub
		
	}
}
