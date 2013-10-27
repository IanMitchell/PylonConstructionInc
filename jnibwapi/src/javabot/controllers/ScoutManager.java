package javabot.controllers;
public class ScoutManager {
	private static ScoutManager instance = null;
	
	private ScoutManager() {
		
	}
	
	public static ScoutManager getInstance() {
		if(instance == null) {
			instance = new ScoutManager();
		}
		return instance;
	}

}
