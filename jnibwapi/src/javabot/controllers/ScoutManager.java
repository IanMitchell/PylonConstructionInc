package javabot.controllers;

import javabot.JavaBot;
import javabot.models.Unit;


public class ScoutManager implements Manager {
	private static ScoutManager instance = null;
	
	private static Unit scout = null;
	
	private ScoutManager() {
		
	}
	
	public static ScoutManager getInstance() {
		if(instance == null) {
			instance = new ScoutManager();
		}
		return instance;
	}
	
	public static void setScout(Unit unit) {
		scout = unit;
	}

	@Override
	public void act() {
		if (scout == null) {
			//Inform JavaBot you have no scout
			JavaBot.needsScout();
		}
		else {
			//Do scout-y stuff
		}
		// TODO Auto-generated method stuff	
	}

	@Override
	public void gameUpdate() {
		// TODO Auto-generated method stub
		
	}

}
