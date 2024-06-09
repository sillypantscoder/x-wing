package com.sillypantscoder.xwing;

import java.util.ArrayList;
import java.util.Arrays;

public class Player {
	public Game game;
	public String name;
	public Ship[] ships;
	public boolean ready;
	public ArrayList<String> events;
	public Player(Game game, String name, Ship[] ships) {
		this.game = game;
		this.name = name;
		this.ships = ships;
		this.ready = false;
		this.events = new ArrayList<String>();
	}
	public void fire(String event) {
		events.add(event);
	}
	public String getEvents() {
		String r = String.join("\n\n", this.events.toArray(new String[0]));
		this.events.clear();
		return r;
	}
	public void addShip(Ship s) {
		Ship[] newships = new Ship[this.ships.length + 1];
		for (int i = 0; i < this.ships.length; i++) {
			newships[i] = this.ships[i];
		}
		newships[this.ships.length] = s;
		this.ships = newships;
	}
	public void resetEvents() {
		events.clear();
		// Load all the players
		for (int i = 0; i < this.game.players.size(); i++) {
			Player p = this.game.players.get(i);
			this.fire("addplayer\n" + p.name);
			for (int s = 0; s < p.ships.length; s++) {
				Ship ship = p.ships[s];
				int type = Arrays.asList(ShipType.types).indexOf(ship.type);
				this.fire("addship\n" + p.name + "\n" + type + "\n" + ship.pos.x + "\n" + ship.pos.y + "\n" + ship.rotation + "\n" + ship.id);
			}
		}
	}
	public Ship getShipByID(int id) {
		for (int i = 0; i < this.ships.length; i++) {
			if (this.ships[i].id == id) {
				return this.ships[i];
			}
		}
		return null;
	}
}
