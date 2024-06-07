package com.sillypantscoder.xwing;

import java.util.Arrays;
import java.util.stream.Stream;

// import java.io.File;

// import com.fasterxml.jackson.core.JsonFactory;
// import com.fasterxml.jackson.core.json.JsonReadFeature;
// import com.fasterxml.jackson.databind.ObjectMapper;

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
	// public static void main(String[] args) throws Exception {
	// 	ObjectMapper mapper = new ObjectMapper();
	// 	ShipType value = mapper.readValue(new File(args[0]), ShipType.class);
	// 	System.out.println(value.toString());
	// }
	public static ShipType[] getTypes() {
		String[] info = Utils.readFile("ships.txt").split("\n\n");
		ShipType[] parsed = new ShipType[info.length];
		for (var i = 0; i < info.length; i++) {
			String[] lines = info[i].split("\n");
			String shipname = lines[0];
			String pilotname = lines[1];
			Integer[] points = Stream.of(lines[2].split(", ")).map((x) -> Integer.parseInt(x.split(" ")[1])).toArray(Integer[]::new);
			Maneuver[] maneuvers = Maneuver.parseSet(String.join("\n", Arrays.copyOfRange(lines, 3, lines.length - 1)));
			int size = Integer.parseInt(lines[lines.length - 1].substring(5));
			parsed[i] = new ShipType(shipname, pilotname, points[0], points[1], points[2], points[3], points[4], maneuvers, size);
		}
		return parsed;
	}
}
