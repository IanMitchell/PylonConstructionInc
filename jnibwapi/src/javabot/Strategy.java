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
import javabot.models.Race;
import javabot.models.UpgradeBuild;
import javabot.types.UnitType.UnitTypes;
import javabot.types.UpgradeType.UpgradeTypes;
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
	private  String strategy;
	private  int race = -1;
	
	public Strategy() {
		initialPriorityList = new ArrayDeque<BuildTime>();
		unitPriorityList = new LinkedList<UnitTypes>();
		buildingPriorityList = new ArrayDeque<UnitTypes>();
		upgradePriorityList = new ArrayDeque<UpgradeBuild>();
		possibleStrats = new ArrayList<String>();
		strategy = null;
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
	
	public void loadSelection(int enemyRaceID) {
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode rootNode = m.readTree(new File("strategies", "StrategySelection.json"));
			String enemyRace;
			
			switch (enemyRaceID) {
			case 0:
				enemyRace = "Zerg";
				break;
			case 1:
				enemyRace = "Terran";
				break;
			case 2:
				enemyRace = "Protoss";
				break;
			default:
				enemyRace = "Random";
			}
			
			System.out.println("Enemy: " + enemyRace);
			
			possibleStrats = new ArrayList<String>();
			for (JsonNode element : rootNode.path(enemyRace)) {
				possibleStrats.add(element.textValue());
			}
		} catch (Exception e) {
			JavaBot.bwapi.printText("I broke");
			e.printStackTrace();
		}
	}
	
	public void loadStrategy(int enemyRaceID) {
		Random rand = new Random();
		
		loadSelection(enemyRaceID);
		
		strategy = possibleStrats.get(rand.nextInt(possibleStrats.size()));
		
		try {
			loadStrategy(strategy);
			JavaBot.bwapi.printText(" ==== Running Strategy: " + strategy + " ==== ");
		} catch (Exception e) {
			JavaBot.bwapi.printText("I broke");
			e.printStackTrace();
		}
		
		printStrategy();
	}
	
	public void loadStrategy(String strategy) throws JsonProcessingException, IOException {
		ObjectMapper m = new ObjectMapper();
		JsonNode rootNode = m.readTree(new File("strategies", strategy + ".json"));
		
		initialPriorityList = new ArrayDeque<BuildTime>();
		unitPriorityList = new LinkedList<UnitTypes>();
		buildingPriorityList = new ArrayDeque<UnitTypes>();
		upgradePriorityList = new ArrayDeque<UpgradeBuild>();
		
		for (JsonNode element : rootNode.path("initialPriorityList")) {
			ObjectNode priority = (ObjectNode)element;
			String type = priority.path("type").textValue();
			if (type == null) {
				initialPriorityList.add(new BuildTime(priority.path("supplyNum").asInt(),
						UnitTypes.valueOf(priority.path("name").textValue())));
			} else if (type.equals("upgrade")) {
				initialPriorityList.add(new BuildTime(priority.path("supplyNum").asInt(),
						new UpgradeBuild(UpgradeTypes.valueOf(priority.path("name").textValue()),
								UnitTypes.valueOf(priority.path("building").textValue()))));
			} else {
				System.out.printf("Found unknown 'type':'%s' in initialPriorityList for %s.json\n", type, strategy);
			}
			
		}
		
		for (JsonNode element : rootNode.path("unitPriorityList")) {
			ObjectNode priority = (ObjectNode)element;
			int count = priority.path("count").asInt() != 0 ? priority.path("count").asInt() : 1;
			
			for (int i = 0; i < count; i++) {
				unitPriorityList.add(UnitTypes.valueOf(priority.path("name").textValue()));
			}
		}
		
		for (JsonNode element : rootNode.path("buildingPriorityList")) {
			ObjectNode priority = (ObjectNode)element;
			int count = priority.path("count").asInt() != 0 ? priority.path("count").asInt() : 1;
			
			for (int i = 0; i < count; i++) {
				buildingPriorityList.add(UnitTypes.valueOf(priority.path("name").textValue()));
			}
		}
		
		for (JsonNode element : rootNode.path("upgradePriorityList")) {
			ObjectNode priority = (ObjectNode)element;
			
			upgradePriorityList.add(new UpgradeBuild(UpgradeTypes.valueOf(priority.path("name").textValue()),
					UnitTypes.valueOf(priority.path("building").textValue())));
		}
	}
	
	private void printStrategy()
	{
		System.out.println("\t\t" + strategy);
		System.out.println("\tinitialPriorityList:");
		for (BuildTime bt : initialPriorityList) {
			if (bt.getUnit() != null) {
				System.out.printf("%d : %s\n", bt.getSupplyNum(), bt.getUnit());
			} else if (bt.getUpgrade() != null) {
				System.out.printf("%d : %s @ %s\n", bt.getSupplyNum(), bt.getUpgrade().getUpgrade(), bt.getUpgrade().getBuilding());
			}
		}

		System.out.println("\tunitPriorityList:");
		for (UnitTypes ut : unitPriorityList) {
			System.out.printf("%s\n", ut);
		}
		
		System.out.println("\tbuildingPriorityList:");
		for (UnitTypes ut : buildingPriorityList) {
			System.out.printf("%s\n", ut);
		}

		System.out.println("\tupgradePriorityList:");
		for (UpgradeBuild ub : upgradePriorityList) {
			System.out.printf("%s @ %s\n", ub.getUpgrade(), ub.getBuilding());
		}
	}
	
	public void setRace(int raceID)
	{
		// This is where you set the race. 
		// Use the Race enum from javabot.models to get the ID!
	}
	
	public int getRace()
	{
		return race;
	}
	public UnitTypes TypetoTypes(int typeID) {
		if(typeID == UnitTypes.Protoss_Arbiter.ordinal())
			return UnitTypes.Protoss_Arbiter;
		else if(typeID == UnitTypes.Protoss_Arbiter_Tribunal.ordinal())
			return UnitTypes.Protoss_Arbiter_Tribunal;
		else if(typeID == UnitTypes.Protoss_Archon.ordinal())
			return UnitTypes.Protoss_Archon;
		else if(typeID == UnitTypes.Protoss_Assimilator.ordinal())
			return UnitTypes.Protoss_Assimilator;
		else if(typeID == UnitTypes.Protoss_Carrier.ordinal())
			return UnitTypes.Protoss_Carrier;
		else if(typeID == UnitTypes.Protoss_Citadel_of_Adun.ordinal())
			return UnitTypes.Protoss_Citadel_of_Adun;
		else if(typeID == UnitTypes.Protoss_Corsair.ordinal())
			return UnitTypes.Protoss_Corsair;
		else if(typeID == UnitTypes.Protoss_Cybernetics_Core.ordinal())
			return UnitTypes.Protoss_Cybernetics_Core;
		else if(typeID == UnitTypes.Protoss_Dark_Archon.ordinal())
			return UnitTypes.Protoss_Dark_Archon;
		else if(typeID == UnitTypes.Protoss_Dark_Templar.ordinal())
			return UnitTypes.Protoss_Dark_Templar;
		else if(typeID == UnitTypes.Protoss_Dragoon.ordinal())
			return UnitTypes.Protoss_Dragoon;
		else if(typeID == UnitTypes.Protoss_Fleet_Beacon.ordinal())
			return UnitTypes.Protoss_Fleet_Beacon;
		else if(typeID == UnitTypes.Protoss_Forge.ordinal())
			return UnitTypes.Protoss_Forge;
		else if(typeID == UnitTypes.Protoss_Gateway.ordinal())
			return UnitTypes.Protoss_Gateway;
		else if(typeID == UnitTypes.Protoss_High_Templar.ordinal())
			return UnitTypes.Protoss_High_Templar;
		else if(typeID == UnitTypes.Protoss_Interceptor.ordinal())
			return UnitTypes.Protoss_Interceptor;
		else if(typeID == UnitTypes.Protoss_Nexus.ordinal())
			return UnitTypes.Protoss_Nexus;
		else if(typeID == UnitTypes.Protoss_Observatory.ordinal())
			return UnitTypes.Protoss_Observatory;
		else if(typeID == UnitTypes.Protoss_Observer.ordinal())
			return UnitTypes.Protoss_Observer;
		else if(typeID == UnitTypes.Protoss_Photon_Cannon.ordinal())
			return UnitTypes.Protoss_Photon_Cannon;
		else if(typeID == UnitTypes.Protoss_Probe.ordinal())
			return UnitTypes.Protoss_Probe;
		else if(typeID == UnitTypes.Protoss_Pylon.ordinal())
			return UnitTypes.Protoss_Pylon;
		else if(typeID == UnitTypes.Protoss_Reaver.ordinal())
			return UnitTypes.Protoss_Reaver;
		else if(typeID == UnitTypes.Protoss_Robotics_Facility.ordinal())
			return UnitTypes.Protoss_Robotics_Facility;
		else if(typeID == UnitTypes.Protoss_Robotics_Support_Bay.ordinal())
			return UnitTypes.Protoss_Robotics_Support_Bay;
		else if(typeID == UnitTypes.Protoss_Scarab.ordinal())
			return UnitTypes.Protoss_Scarab;
		else if(typeID == UnitTypes.Protoss_Scout.ordinal())
			return UnitTypes.Protoss_Scout;
		else if(typeID == UnitTypes.Protoss_Shield_Battery.ordinal())
			return UnitTypes.Protoss_Shield_Battery;
		else if(typeID == UnitTypes.Protoss_Shuttle.ordinal())
			return UnitTypes.Protoss_Shuttle;
		else if(typeID == UnitTypes.Protoss_Stargate.ordinal())
			return UnitTypes.Protoss_Stargate;
		else if(typeID == UnitTypes.Protoss_Templar_Archives.ordinal())
			return UnitTypes.Protoss_Templar_Archives;
		else if(typeID == UnitTypes.Protoss_Zealot.ordinal())
			return UnitTypes.Protoss_Zealot;
		else
			return null;
	}

}
