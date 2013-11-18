package javabot.models;

import javabot.types.UnitType.UnitTypes;

public class BuildTime {
	private int supplyNum;
	private UnitTypes unit;
	private UpgradeBuild upgrade;
	
	/* for Buildings and Units */
	public BuildTime(int supply, UnitTypes u) {
		supplyNum = supply;
		unit = u;
		upgrade = null;
	}
	
	/* for Upgrades */
	public BuildTime(int supply, UpgradeBuild u) {
		supplyNum = supply;
		unit = null;
		upgrade = u;
	}
	
	public int getSupplyNum() {return supplyNum;}
	public UnitTypes getUnit() {return unit;}
	public UpgradeBuild getUpgrade() {return upgrade;}
}
