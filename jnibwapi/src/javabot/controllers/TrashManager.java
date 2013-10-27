package javabot.controllers;
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

}
