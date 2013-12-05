package javabot.controllers;

import javabot.models.Unit;

public class TrashManager {
	private static TrashManager instance = null;
	
	private TrashManager() {
		
	}
	
	public static TrashManager getInstance() {
		if(instance == null) {
			instance = new TrashManager();
		}
		return instance;
	}
	
	public void printMessage(String msg) {
		JavaBot.bwapi.printText(msg);
	}

}
