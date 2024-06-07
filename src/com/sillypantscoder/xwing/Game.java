package com.sillypantscoder.xwing;

import java.util.ArrayList;

public class Game {
	public ArrayList<Player> players;
	public ArrayList<Ship> ships;
	public GameStatus status;
	public Player turn;
	public Game() {
		players = new ArrayList<Player>();
		ships = new ArrayList<Ship>();
		status = GameStatus.PLANNING;
		turn = null;
		this.login("jason");
	}
	public void login(String name) {
		// ShipType[] types = ShipType.getTypes();
		// String[] indexes = ships.split("\n");
		// Ship[] parsed = new Ship[indexes.length];
		// for (int i = 0; i < indexes.length; i++) {
		// 	int type = Integer.parseInt(indexes[i]);
		// 	Ship newShip = new Ship(this, types[type], new Point(0, 0), 0);
		// 	parsed[i] = newShip;
		// 	this.ships.add(newShip);
		// }
		this.players.add(new Player(this, name, new Ship[] {}));
		// Notify everyone
		for (int i = 0; i < this.players.size(); i++) {
			this.players.get(i).fire("addplayer\n" + name);
		}
	}
	public static enum GameStatus {
		PLANNING,
		MOVING,
		COMBAT
	}
}
