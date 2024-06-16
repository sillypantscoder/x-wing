package com.sillypantscoder.xwing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

// import java.io.File;

// import com.fasterxml.jackson.core.JsonFactory;
// import com.fasterxml.jackson.core.json.JsonReadFeature;
// import com.fasterxml.jackson.databind.ObjectMapper;

public class ShipType {
	public static ShipType[] types = getTypes();
	public Team team;
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
	public ShipType(Team team, String shipName, String pilotName, int skill, int attackAmount, int defendAmount, int hullValue, int shieldValue, Maneuver[] maneuvers, int size) {
		this.team = team;
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
		ArrayList<ShipType> parsed = new ArrayList<ShipType>();
		Team currentTeam = null;
		for (var i = 0; i < info.length; i++) {
			String[] lines = info[i].split("\n");
			if (lines[0].startsWith("TEAM")) {
				// Switch teams
				String name = lines[0].substring(5);
				String[] colors = lines[1].substring(8).split(" ");
				currentTeam = new Team(name, colors);
			} else {
				// Ship entry
				String shipname = lines[0];
				String pilotname = lines[1];
				Integer[] points = Stream.of(lines[2].split(", ")).map((x) -> Integer.parseInt(x.split(" ")[1])).toArray(Integer[]::new);
				Maneuver[] maneuvers = Maneuver.parseSet(String.join("\n", Arrays.copyOfRange(lines, 3, lines.length - 1)));
				int size = Integer.parseInt(lines[lines.length - 1].substring(5));
				if (currentTeam == null) throw new Error("Ship entry came before team definition in ships.txt file");
				parsed.add(new ShipType(currentTeam, shipname, pilotname, points[0], points[1], points[2], points[3], points[4], maneuvers, size));
			}
		}
		return Utils.arrayListToArray(parsed, ShipType.class);
	}
}
