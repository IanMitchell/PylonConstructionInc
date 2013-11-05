package javabot.controllers;

import javabot.models.Unit;

public interface Manager {

	//called once per second
	public void act();
	
	//called approximated 30x per second
	public void gameUpdate();
	
	//adds unit to the Manager
	public void assignUnit(Unit unit);
	
	/**
	 * Removes unit from Manager
	 * @param unitId id of unit
	 * @return id of removed unit
	 */
	public int removeUnit(int unitId);
}
