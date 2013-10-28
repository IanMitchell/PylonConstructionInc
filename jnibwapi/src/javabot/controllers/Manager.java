package javabot.controllers;

public interface Manager {

	//called every 2x per second
	public void act();
	
	//called once per second
	public void gameUpdate();
}
