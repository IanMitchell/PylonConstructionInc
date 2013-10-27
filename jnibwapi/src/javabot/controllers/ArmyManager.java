package javabot.controllers;
import javabot.models.*;

public class ArmyManager {
	private static ArmyManager instance = null;
	
	private ArmyManager() {
		
	}
	
	public static ArmyManager getInstance() {
		if(instance == null) {
			instance = new ArmyManager();
		}
		return instance;
	}
}
