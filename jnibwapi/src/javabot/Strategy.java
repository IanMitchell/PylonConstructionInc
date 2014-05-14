package javabot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javabot.models.BuildTime;
import javabot.models.UpgradeBuild;
import javabot.types.UnitType.UnitTypes;
import javabot.Strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class Strategy {
	
	private  Deque<BuildTime> initialPriorityList;
	private  List<UnitTypes> unitPriorityList;
	private  Deque<UnitTypes> buildingPriorityList;
	private  Deque<UpgradeBuild> upgradePriorityList;
	private  ArrayList<String> possibleStrats;
	
	public Strategy() {
		initialPriorityList = new ArrayDeque<BuildTime>();
		unitPriorityList = new LinkedList<UnitTypes>();
		buildingPriorityList = new ArrayDeque<UnitTypes>();
		upgradePriorityList = new ArrayDeque<UpgradeBuild>();
		possibleStrats = new ArrayList<String>();
	}
	
	public Deque<BuildTime> getInitialList() {
		return this.initialPriorityList;
	}
	
	public List<UnitTypes> getUnitList() {
		return this.unitPriorityList;
	}
	
	public Deque<UnitTypes> getBuildingList() {
		return this.buildingPriorityList;
	}
	
	public Deque<UpgradeBuild> getUpgradeList() {
		return this.upgradePriorityList;
	}
	
	// maybe hardcode strategies into list?
	public void addStrategy(String name) {
		possibleStrats.add(name);
	}
		
	public void pickStrategy(String name) {
		String strategy;
		
		if(possibleStrats.contains(name)) {
			strategy = name;
		}
		else {
			Random rand = new Random();
			strategy = possibleStrats.get(rand.nextInt(possibleStrats.size()));
		}
		
		try {
			loadStrategy(strategy);
			JavaBot.bwapi.printText(" ==== Running Strategy: " + strategy + " ==== ");
		} catch (Exception e) {
			JavaBot.bwapi.printText("I broke");
			e.printStackTrace();
		}
	}
	
	public void loadStrategy(String strategy) throws JsonProcessingException, IOException {
		ObjectMapper m = new ObjectMapper();
		JsonNode rootNode = m.readTree(new File("strategies", strategy + ".json"));
		
		for (JsonNode element : rootNode.path("initialPriorityList")) {
			ObjectNode priority = (ObjectNode)element;
			initialPriorityList.add(new BuildTime(priority.path("time").intValue(), UnitTypes.valueOf(priority.path("name").textValue())));
		}
		
		for (JsonNode element : rootNode.path("unitPriorityList")) {
			ObjectNode priority = (ObjectNode)element;
			unitPriorityList.add(UnitTypes.valueOf(priority.path("name").textValue()));
		}
		
		for (JsonNode element : rootNode.path("buildingPriorityList")) {
			ObjectNode priority = (ObjectNode)element;
			buildingPriorityList.add(UnitTypes.valueOf(priority.path("name").textValue()));
		}
	}
	

}
