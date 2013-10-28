package javabot.controllers;

public interface Manager {

	//called once per second
	public void act();
	
	//called approximated 30x per second
	public void gameUpdate();
}
