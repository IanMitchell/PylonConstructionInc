package javabot.controllers;
public class ScoutManager implements Manager {
	private static ScoutManager instance = null;
	
	private ScoutManager() {
		
	}
	
	public static ScoutManager getInstance() {
		if(instance == null) {
			instance = new ScoutManager();
		}
		return instance;
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
