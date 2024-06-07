package com.sillypantscoder.xwing;

import java.util.Optional;

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
	// Constructor
	public Ship(Game game, ShipType type, Point pos, int r) {
		this.game = game;
		this.type = type;
		this.pos = pos;
		this.rotation = r;
		this.stress = 0;
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
	public static void main(String[] args) {
		Game game = new Game();
		ShipType testship = ShipType.getTypes()[0];
		// Create a ship
		Ship ship = new Ship(game, testship, new Point(0, 0), 0);
		game.ships.add(ship);
		System.out.println(ship);
		System.out.println(ship.getRect());
		// Create another ship
		Ship ship2 = new Ship(game, testship, new Point(30, -50), 45);
		game.ships.add(ship2);
		System.out.println(ship2.getRect());
		// List maneuvers
		for (int i = 0; i < ship2.type.maneuvers.length; i++) {
			System.out.println(i + ": " + ship2.type.maneuvers[i].toString());
		}
		// Test a maneuver
		Maneuver m = ship2.type.maneuvers[10];
		Optional<Runnable> result = m.execute(ship2);
		result.ifPresentOrElse((r) -> {
			System.out.println(ship2);
			r.run();
			System.out.println(ship2);
		}, () -> {
			System.out.println(ship2);
			System.out.println("[No change]");
		});
		System.out.println("\nSecond maneuver...\n");
		// Test another maneuver
		Maneuver m2 = ship.type.maneuvers[11];
		Optional<Runnable> result2 = m2.execute(ship);
		result2.ifPresentOrElse((r) -> {
			System.out.println(ship);
			r.run();
			System.out.println(ship);
		}, () -> {
			System.out.println(ship);
			System.out.println("[No change]");
		});
	}
}
