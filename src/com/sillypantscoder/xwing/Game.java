package com.sillypantscoder.xwing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Game {
	public ArrayList<Player> players;
	public ArrayList<Ship> ships;
	public GameStatus status;
	private ArrayList<Ship> shipOrder;
	private int activeShipIndex; // in shipOrder 

	private int nextShipId;

	public Game() {
		players = new ArrayList<Player>();
		ships = new ArrayList<Ship>();
		status = GameStatus.STARTING;
		nextShipId = 1;
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
		this.broadcast("addplayer\n" + name);
	}
	public void broadcast(String event) {
		for (int i = 0; i < this.players.size(); i++) {
			this.players.get(i).fire(event);
		}
	}
	public void startMovingPhase() {
		// Logic to do here:
		// - Update game status to "moving"
		this.status = GameStatus.MOVING;
		this.broadcast("we dun started moving stuff, yo!");
		// - Determine the order that ships will be moving in. Have a reliable way
		//   to settle ties since that will be common. In the actual game, skill
		//   ties between the empire and rebels are settled by alternating which
		//   side gets to go and the player gets to choose which ship. That's
		//   complicated, so I think maybe we should do the alternating but
		//   automatically pick which ship gets selected.
		this.shipOrder = this.computeShipOrderForMovement();
		// - For each ship in order, execute the selected maneuver and allow the
		//   player to pick an action.
		this.activeShipIndex = -1;
		this.moveNextShip();
	}
	public ArrayList<Ship> computeShipOrderForMovement() {
		// TODO: make this correct (instead of the travesty augusto has here)
		return this.ships;
	}
	private Ship getActiveShip() {
		if (this.activeShipIndex >= this.shipOrder.size()) {
			return null;
		}
		return this.shipOrder.get(this.activeShipIndex);
	}
	/**
	 * Set the next ship to be activated.
	 * @return the newly active ship or null if we ran out of ships.
	 */
	private Ship activateNextShip() {
		this.activeShipIndex++;
		return this.getActiveShip();
 	}
	private void moveNextShip() {
		Ship activeShip = this.activateNextShip();
		if (activeShip == null) {
			this.beginCombatPhase(); // :O
			return;
		}
		// Move the ship
		Optional<Runnable> moveShip = activeShip.maneuver.execute(activeShip);
		moveShip.ifPresent((v) -> v.run());
		// Find which player owns the active ship
		this.broadcast("Move ship\n" + activeShip.id);
		// Now wait for the player to send us the action after we've sent the status.
	}
	public Player getPlayerForShip(Ship ship) {
		// TODO: insert programming
		throw new Error("we are lazy :(");
	}
	public void selectActionForActiveShip(int shipID, Action action) {
		Ship activeShip = this.getActiveShip();
		if (activeShip.id != shipID) {
			// the client is untrustworthy
			throw new Error("client is suspicious and has sent us untrustworthy things...");
		}
		activeShip.action = action;
		this.moveNextShip();
	}
	public Player getPlayerByName(String playerName) {
		List<Player> targets = this.players.stream().filter((x) -> x.name.equals(playerName)).toList();
		Player target = targets.get(0);
		return target;
	}
	private void beginCombatPhase() {
		this.status = GameStatus.COMBAT;
		this.broadcast("we dun started FIGHTIN! 💥🤯  pew pew");
	}
	public void markReady(String playerName) {
		Player target = getPlayerByName(playerName);
		// is ready :)
		target.ready = true;
		// Are we all ready?
		boolean allready = true;
		for (int i = 0; i < this.players.size(); i++) {
			if (this.players.get(i).ready == false) allready = false;
		}
		if (allready) {
			// reset ready for all players
			for (int i = 0; i < this.players.size(); i++) {
				this.players.get(i).ready = false;
			}
			// start the next phase, whichever that is.
			if (this.status == GameStatus.STARTING) this.startPlanningPhase();
			else if (this.status == GameStatus.PLANNING) this.startMovingPhase();
			else {
				System.out.println("state is incorrect: player '" + playerName + "' marked ready (and all players are ready) when server status is " + this.status.name());
			}
		}
	}
	public void startPlanningPhase() {
		this.status = GameStatus.PLANNING;
		// TODO: ummmmmmmmm
		this.broadcast("Why did the chicken cross the road? To get to the other side!");
	}
	public void addShip(Player target, int shipIndex) {
		// ship
		ShipType type = ShipType.types[shipIndex];
		Ship s = new Ship(this, type, new Point(Random.randint(0, 1000), Random.randint(0, 1000)), Random.randint(0, 360), this.nextShipId);
		this.nextShipId++;
		// add the ship
		target.addShip(s);
		this.ships.add(s);
		// notify everyone
		this.broadcast("addship\n" + target.name + "\n" + shipIndex + "\n" + s.pos.x + "\n" + s.pos.y + "\n" + s.rotation + "\n" + s.id);
	}
	public static enum GameStatus {
		STARTING,
		PLANNING,
		MOVING,
		COMBAT
	}
}
