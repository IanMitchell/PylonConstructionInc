package javabot.controllers;
public class TrashManager implements Manager {
	private static TrashManager instance = null;
	
	private TrashManager() {
		
	}
	
	public static TrashManager getInstance() {
		if(instance == null) {
			instance = new TrashManager();
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
