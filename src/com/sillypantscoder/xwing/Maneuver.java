package com.sillypantscoder.xwing;

import java.util.ArrayList;
import java.util.Optional;

public class Maneuver {
	public int speed;
	public int angle;
	public int stress;
	// Add the ones where you rotate the ship 90 degrees at the end
	public Maneuver(int speed, int angle, int stress) {
		this.speed = speed;
		this.angle = angle;
		this.stress = stress;
	}
	/**
	 * Attempt to execute this maneuver on a ship. If it is not possible for the ship to execute the maneuver, return null. Otherwise, return a Runnable that actually moves the ship.
	 * @param ship
	 * @return
	 */
	public Optional<Runnable> execute(Ship ship) {
		// 1. Check if the ship is stressed
		if (ship.isStressed() && this.stress >= 1) return Optional.empty(); // aaaa!
		// 2. Get the new location of the ship
		Point newLocation = ship.pos.moveDirection(ship.rotation + (this.angle / 2), this.speed * 50);
		Rect newRect = new Rect(newLocation.x, newLocation.y, ship.type.size, ship.rotation + angle);
		for (int i = 0; i < ship.game.ships.size(); i++) {
			Ship other = ship.game.ships.get(i);
			if (other == ship) continue;
			if (other.collidesWith(newRect)) {
				// System.out.println("[Collision: ship collides with ship #" + i + "]");
				return Optional.empty(); // bonk!
			}
		}
		return Optional.ofNullable(() -> {
			ship.pos = newLocation;
			ship.rotation += angle;
			ship.stress += stress;
			if (ship.stress < 0) ship.stress = 0;
		});
	}
	public String toString() {
		return "Maneuver { speed: " + speed + ", angle: " + angle + ", stress: " + stress + " }";
	}
	public static Maneuver[] parseSet(String set) {
		// The set will look like this:             This will map to the list of all possible maneuvers, which looks like this:
		// /-----\                                    /-----\
		// |  +  |     (the +, -, and = indicate    4 |<\^/>|   (< and > are right angle turns)
		// |=====|      whether to add or remove    3 |<\^/>|   (\ and / are 45-degree turns)
		// |==-==|      a stress token)             2 |<\^/>|   (^ is forwards)
		// | --- |                                  1 |<\^/>|   (numbers on left indicate speed)
		// \-----/                                    \-----/
		// To parse this, we need to go through each line of the string in reverse order.
		ArrayList<Maneuver> maneuvers = new ArrayList<Maneuver>();
		String[] rows = set.split("\n");
		int speed = 1;
		for (int i = rows.length - 1; i >= 0; i--) {
			// Now go through the columns.
			for (int c = 0; c < rows[i].length(); c++) {
				// Check if there is a maneuver here.
				char m = rows[i].charAt(c);
				if (m == ' ') continue;
				// Extract the stress value.
				int stress = 0;
				if (m == '+') stress = 1;
				else if (m == '-') stress = -1;
				else if (m == '=') stress = 0;
				else throw new RuntimeException("Bad maneuver char '" + m + "' on line " + i + " at position " + c + "!!!!!!!!!!!!!!!");
				// Extract the angle.
				int angle = (c - 2) * 45;
				// Register the maneuver.
				maneuvers.add(new Maneuver(speed, angle, stress));
			}
			// Every row, the speed increases.
			speed += 1;
		}
		return Utils.arrayListToArray(maneuvers, Maneuver.class);
	}
}
