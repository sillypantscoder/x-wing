package com.sillypantscoder.xwing;

import java.util.ArrayList;

public class Player {
	public Game game;
	public String name;
	public Team team;
	public Ship[] ships;
	public boolean ready;
	public ArrayList<String> events;
	public Player(Game game, String name, Team team, Ship[] ships) {
		this.game = game;
		this.name = name;
		this.team = team;
		this.ships = ships;
		this.ready = false;
		this.events = new ArrayList<String>();
	}
	public void fire(String event) {
		events.add(event);
	}
	public void fire(Event event) {
		event.fire(this); // this will turn around and call fire(String) after calling Event.fire(Player)
	}
	public String getEvents() {
		String r = String.join("\n\n", this.events.toArray(new String[0]));
		this.events.clear();
		return r;
	}
	public void setReady(boolean ready) {
		this.ready = ready;
		game.broadcast(new Event.PlayerReady(this, ready));
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
		this.game.sendNewConnectionEvents(this);
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
