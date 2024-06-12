package com.sillypantscoder.xwing;

import java.util.Arrays;

public class Ship {
	public Game game;
	public ShipType type;
	/**
	 * The location of the center of this ship
	 */
	public Point pos;
	public int rotation;
	// Tokens
	public int stress;
	public int id;

	public Maneuver maneuver;
	public Action action;

	// Constructor
	public Ship(Game game, ShipType type, Point pos, int r, int id) {
		this.game = game;
		this.type = type;
		this.pos = pos;
		this.rotation = r;
		this.stress = 0;
		this.id = id;
		this.maneuver = null;
		this.action = null;
	}
	public Rect getRect() {
		return new Rect(this.pos.x, this.pos.y, this.type.size, this.rotation);
	}
	public boolean collidesWith(Rect other) {
		return this.getRect().collidesWith(other);
	}
	public boolean isStressed() {
		return this.stress >= 1;
	}
	public String toString() {
		return "Ship\n\t| type: " + type.toString().replace("\n", "\n\t| ") + "\n\t| pos: " + pos.toString() + "\n\t| rotation: " + rotation + "\n\t| stress: " + stress;
	}
	public int pleaseGiveMeTheManeuverIndexSoIDontMakeMyEyesBleedWithTheAbsoluteAbominationOfDoingItElsewhere() {
		return Arrays.asList(this.type.maneuvers).indexOf(this.maneuver); // 😭
	}
	public String pleaseGiveMeTheManeuverIndexAsAStringSoIDontMakeMyEyesBleedWithTheAbsoluteAbominationOfDoingItElsewhere() {
		return ("terriblestringconversion" + this.pleaseGiveMeTheManeuverIndexSoIDontMakeMyEyesBleedWithTheAbsoluteAbominationOfDoingItElsewhere()).substring(24);
	}
	public static void main(String[] args) {
		Game game = new Game();
		ShipType testship = ShipType.types[0];
		// Create a ship
		Ship ship = new Ship(game, testship, new Point(0, 0), 0, 1);
		game.ships.add(ship);
		System.out.println(ship);
		System.out.println(ship.getRect());
		// Create another ship
		Ship ship2 = new Ship(game, testship, new Point(30, -50), 45, 2);
		game.ships.add(ship2);
		System.out.println(ship2.getRect());
		// List maneuvers
		for (int i = 0; i < ship2.type.maneuvers.length; i++) {
			System.out.println(i + ": " + ship2.type.maneuvers[i].toString());
		}
		// Test a maneuver
		Maneuver m = ship2.type.maneuvers[10];
		Maneuver.Result result = m.compute(ship2);
		if (!result.failed()) {
			System.out.println(ship2);
			result.apply(ship2);
			System.out.println(ship2);
		} else {
			System.out.println(ship2);
			System.out.println("[No change]");
		};
		System.out.println("\nSecond maneuver...\n");
		// Test another maneuver
		Maneuver m2 = ship.type.maneuvers[11];
		Maneuver.Result result2 = m2.compute(ship);
		if (!result2.failed()) {
			System.out.println(ship);
			result2.apply(ship);
			System.out.println(ship);
		} else {
			System.out.println(ship);
			System.out.println("[No change]");
		};
	}
}
