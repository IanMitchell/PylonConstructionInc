package javabot.models;

import javabot.types.UnitType.UnitTypes;
import javabot.types.UpgradeType;
import javabot.types.UpgradeType.UpgradeTypes;

public class UpgradeBuild {
	private UpgradeTypes upgrade;
	private UnitTypes building;
	
	public UpgradeBuild(UpgradeTypes upgrade, UnitTypes building) {
		this.upgrade = upgrade;
		this.building = building;
	}
	
	public UpgradeTypes getUpgrade() {return upgrade;}
	public UnitTypes getBuilding() {return building;}
}
