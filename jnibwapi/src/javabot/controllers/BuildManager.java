package javabot.controllers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import javabot.JavaBot;
import javabot.models.Unit;
import javabot.models.UpgradeBuild;
import javabot.types.UnitType;
import javabot.types.UnitType.UnitTypes;
import javabot.types.UpgradeType;

public class BuildManager implements Manager {
	private static BuildManager instance = null;
	
	private ArrayList<Integer> buildingsBeingConstructed;
	private ArrayList<Integer> buildings;
	private LinkedList<UnitTypes> buildOrder;
	private LinkedList<UnitTypes> unitOrder;
	private LinkedList<UpgradeBuild> upgradeOrder;
	public boolean workerMovingToBuild = false;
	
	private BuildManager() {
	}
	
	public static BuildManager getInstance() {
		if(instance == null) {
			instance = new BuildManager();
		}
		return instance;
	}
	
	public void reset() {
		buildingsBeingConstructed = new ArrayList<Integer>(); //integer represents unitId
		buildings = new ArrayList<Integer>();
		buildOrder = new LinkedList<UnitTypes>();
		upgradeOrder = new LinkedList<UpgradeBuild>();
		unitOrder = new LinkedList<UnitTypes>();
	}
	
	@Override
	public void act() {
		for (Integer inConstruction : buildingsBeingConstructed) {
			if (JavaBot.bwapi.getUnit(inConstruction).isCompleted()) {
				buildingsBeingConstructed.remove(inConstruction);
				buildings.add(inConstruction);
				
				//notify javabot that building is complete
				if (JavaBot.bwapi.getUnit(inConstruction).getTypeID() == UnitTypes.Protoss_Assimilator.ordinal()) {
					JavaBot.buildingComplete(inConstruction);
				}
			}
		}
		if (!buildOrder.isEmpty() && !workerMovingToBuild) {
			UnitTypes nextToBuild = buildOrder.element();
			int worker = getNearestUnit(UnitTypes.Protoss_Probe.ordinal(), JavaBot.homePositionX, JavaBot.homePositionY);
			if (worker != -1) {
				// if we found him, try to select appropriate build tile position for building
				Point buildTile = getBuildTile(worker, nextToBuild.ordinal(), JavaBot.homePositionX, JavaBot.homePositionY);
				
				//build structure if we found a good spot + aren't already building it + can afford it
				if ((buildTile.x != -1) && (!weAreBuilding(nextToBuild.ordinal()) 
				 && canAfford(JavaBot.bwapi.getUnitType(nextToBuild.ordinal())) && !workerMovingToBuild)) {
					workerMovingToBuild = true;
					JavaBot.bwapi.build(worker, buildTile.x, buildTile.y, nextToBuild.ordinal());
					JavaBot.bwapi.printText("ABOUT TO BUILD: " + JavaBot.bwapi.getUnitType(nextToBuild.ordinal()).getName());
				}
				
				//method not called for assimilator. See unitMorph for Javabot
				if (weAreBuilding(nextToBuild.ordinal())) {
					if (JavaBot.initialPriorityList.size() > 0)
						buildOrder.remove();
					else
						JavaBot.buildingPriorityList.remove(buildOrder.remove());
					workerMovingToBuild = false;
				}
			}
		}
		
		if (!upgradeOrder.isEmpty()) {
			UpgradeBuild nextToUpgrade = upgradeOrder.element();
			UpgradeType upgradeInformation = JavaBot.bwapi.getUpgradeType(nextToUpgrade.getUpgrade().ordinal());
			
			
			for (Integer buildingId : buildings) {
				Unit building = JavaBot.bwapi.getUnit(buildingId);
				if (building.getTypeID() == nextToUpgrade.getBuilding().ordinal() && 
				 JavaBot.bwapi.getSelf().getMinerals() >= upgradeInformation.getMineralPriceBase() &&
				 JavaBot.bwapi.getSelf().getGas() >= upgradeInformation.getGasPriceBase()) {
					JavaBot.bwapi.upgrade(buildingId, nextToUpgrade.getUpgrade().ordinal());
				}
				
				if (building.isUpgrading())
					upgradeOrder.remove();
			}
		}
		
		if (!unitOrder.isEmpty()) {
			UnitTypes nextToTrain = unitOrder.element();
			UnitType unitType = JavaBot.bwapi.getUnitType(nextToTrain.ordinal());
			int building = unitType.getWhatBuildID();
			
			for (int i=0; i<buildings.size(); i++) {
				if (JavaBot.bwapi.getUnit(buildings.get(i)).getTypeID() == building && canBuildUnit(buildings.get(i), unitType)) {
					JavaBot.bwapi.train(buildings.get(i), nextToTrain.ordinal());
					break;
				}
			}
		}
	}
	
	
	public void toBuild(UnitTypes building) {
		if (!buildOrder.contains(building))
			buildOrder.add(building);
	}
	
	public void toUpgrade(UpgradeBuild upgrade) {
		upgradeOrder.add(upgrade);
	}
	
	public boolean canTrain(UnitTypes unit) {
		UnitType unitType = JavaBot.bwapi.getUnitType(unit.ordinal());
		int building = unitType.getWhatBuildID();
		
		for (int i=0; i<buildings.size(); i++) {
			if (JavaBot.bwapi.getUnit(buildings.get(i)).getTypeID() == building && JavaBot.bwapi.getUnit(buildings.get(i)).getTrainingQueueSize() == 0) {
				return canBuildUnit(buildings.get(i), unitType);
			}
		}
		return false;
	}
	
	private boolean canBuildUnit(int unitId, UnitType unitType) {
		return (JavaBot.bwapi.getUnit(unitId).getTrainingQueueSize() == 0 &&
				JavaBot.getSupplyAvailable() >= unitType.getSupplyRequired() &&
				canAfford(unitType));
	}
	
	private boolean canAfford(UnitType unitType) {
		return JavaBot.player.getMinerals() >= unitType.getMineralPrice() && 
				JavaBot.player.getGas() >= unitType.getGasPrice();
	}
	
	public void toTrain(UnitTypes unit) {
		unitOrder.add(unit);
	}
	
	
	/**
	 * name is a bit misleading. Returns total count of building (whether being constructed or built)
	 * @param ordinal See UnitTypes.ordinal()
	 * @return
	 */
	public int getBuildingCount(int ordinal) {
		int count = 0;
		for (Integer inConstruction : buildingsBeingConstructed)
			if (JavaBot.bwapi.getUnit(inConstruction).getTypeID() == ordinal)
				count++;
		
		for (Integer built : buildings)
			if (JavaBot.bwapi.getUnit(built).getTypeID() == ordinal)
				count++;
		
		return count;
	}
	
	@Override
	public void gameUpdate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void assignUnit(Unit unit) {
		UnitType type = JavaBot.bwapi.getUnitType(unit.getTypeID());
		if (type.isBuilding()) {
			if (unit.isBeingConstructed()) {
				workerMovingToBuild = false;
				buildingsBeingConstructed.add(unit.getID());
			}
			else {
				if(buildings == null) {
					JavaBot.bwapi.printText("Buildings is null");
				}
				else {
					buildings.add(unit.getID());
				}
			}
		}
		else if (type.isWorker() || type.isAttackCapable() || type.isSpellcaster())
			unitOrder.remove();
	}
	
	@Override
	public int removeUnit(int unitId) {
		// TODO Auto-generated method stub
		return -1;
	}
	
	// Returns true if we are currently constructing the building of a given type.
	public static boolean weAreBuilding(int buildingTypeID) {
		for (Unit unit : JavaBot.bwapi.getMyUnits()) {
			if ((unit.getTypeID() == buildingTypeID) && (!unit.isCompleted()))
				return true;
			if (JavaBot.bwapi.getUnitType(unit.getTypeID()).isWorker() && unit.getConstructingTypeID() == buildingTypeID)
				return true;
		}
		return false;
	}
	
	// Returns the id of a unit of a given type, that is closest to a pixel position (x,y),
	// ignores gasCarrying workers
	// or -1 if we don't have a unit of this type
    public int getNearestUnit(int unitTypeID, int x, int y) {
    	int nearestID = -1;
	    double nearestDist = 9999999;
	    for (Unit unit : JavaBot.bwapi.getMyUnits()) {
	    	if ((unit.getTypeID() != unitTypeID) || (!unit.isCompleted())) continue;
	    	if (unit.isCarryingGas()) continue;
	    	double dist = Math.sqrt(Math.pow(unit.getX() - x, 2) + Math.pow(unit.getY() - y, 2));
	    	if (nearestID == -1 || dist < nearestDist) {
	    		nearestID = unit.getID();
	    		nearestDist = dist;
	    	}
	    }
	    return nearestID;
    }	
	
	// Returns the Point object representing the suitable build tile position
	// for a given building type near specified pixel position (or Point(-1,-1) if not found)
	// (builderID should be our worker)
	public static Point getBuildTile(int builderID, int buildingTypeID, int x, int y) {
		Point ret = new Point(-1, -1);
		int maxDist = 3;
		int stopDist = 40;
		int tileX = x/32; int tileY = y/32;
		
		// Refinery, Assimilator, Extractor
		if (JavaBot.bwapi.getUnitType(buildingTypeID).isRefinery()) {
			for (Unit n : JavaBot.bwapi.getNeutralUnits()) {
				if ((n.getTypeID() == UnitTypes.Resource_Vespene_Geyser.ordinal()) && 
						( Math.abs(n.getTileX()-tileX) < stopDist ) &&
						( Math.abs(n.getTileY()-tileY) < stopDist )
						) return new Point(n.getTileX(),n.getTileY());
			}
		}
		
		while ((maxDist < stopDist) && (ret.x == -1)) {
			for (int i=tileX-maxDist; i<=tileX+maxDist; i++) {
				for (int j=tileY-maxDist; j<=tileY+maxDist; j++) {
					if (JavaBot.bwapi.canBuildHere(builderID, i, j, buildingTypeID, false)) {
						// units that are blocking the tile
						boolean unitsInWay = false;
						for (Unit u : JavaBot.bwapi.getAllUnits()) {
							if (u.getID() == builderID) continue;
							if ((Math.abs(u.getTileX()-i) < 4) && (Math.abs(u.getTileY()-j) < 4)) unitsInWay = true;
						}
						if (!unitsInWay) {
							ret.x = i; ret.y = j;
							return ret;
						}
						
						// psi power for Protoss (this seems to work out of the box)
						if (JavaBot.bwapi.getUnitType(buildingTypeID).isRequiresPsi()) {}
					}
				}
			}
			maxDist += 2;
		}
		
		if (ret.x == -1)
			JavaBot.bwapi.printText("Unable to find suitable build position for "+JavaBot.bwapi.getUnitType(buildingTypeID).getName());
		return ret;
	}

}
