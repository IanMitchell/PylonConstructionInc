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
	
	public Strategy() {
		initialPriorityList = new ArrayDeque<BuildTime>();
		unitPriorityList = new LinkedList<UnitTypes>();
		buildingPriorityList = new ArrayDeque<UnitTypes>();
		upgradePriorityList = new ArrayDeque<UpgradeBuild>();
		possibleStrats = new ArrayList<String>();
		
		possibleStrats.add("DTRush");
		possibleStrats.add("GoonRush");
		//possibleStrats.add("CarrierRush");
		possibleStrats.add("ZealotRush");
		//possibleStrats.add("CorsairDT");
		//possibleStrats.add("GoonOnlyRush");
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
			initialPriorityList = new ArrayDeque<BuildTime>();
			unitPriorityList = new LinkedList<UnitTypes>();
			buildingPriorityList = new ArrayDeque<UnitTypes>();
			upgradePriorityList = new ArrayDeque<UpgradeBuild>();
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

}
