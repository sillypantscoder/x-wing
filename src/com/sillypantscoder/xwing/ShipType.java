package com.sillypantscoder.xwing;

public class ShipType {
	// stuff about the ship
	public String shipName;
	public String pilotName;
	public int skill;
	public int attackAmount;
	public int defendAmount;
	public int hullValue;
	public int shieldValue;
	public Maneuver[] maneuvers;
	public int size;
	// public Action[] actions;
	// upgrades?
	public ShipType(String shipName, String pilotName, int skill, int attackAmount, int defendAmount, int hullValue, int shieldValue, Maneuver[] maneuvers, int size) {
		this.shipName = shipName;
		this.pilotName = pilotName;
		this.skill = skill;
		this.attackAmount = attackAmount;
		this.defendAmount = defendAmount;
		this.hullValue = hullValue;
		this.shieldValue = shieldValue;
		this.maneuvers = maneuvers;
		this.size = size;
	}
	public String toString() {
		return "ShipType\n\t| name: " + this.shipName + "\n\t| pilot: " + this.pilotName + "\n\t| skill: " + this.skill + "\n\t| attack: " + this.attackAmount +
			"\n\t| defend: " + this.defendAmount + "\n\t| hull: " + this.hullValue + "\n\t| shield: " + this.shieldValue + "\n\t| maneuvers: " + this.maneuvers.length
			+ "\n\t| size: " + this.size;
	}
}
