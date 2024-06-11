package com.sillypantscoder.xwing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		Player newPlayer = new Player(this, name, new Ship[] {});
		this.players.add(newPlayer);
		// Notify everyone
		(new Event.PlayerLogin(newPlayer)).broadcast(this);
	}
	private void setGameStatus(GameStatus status) {
		this.status = status;
		(new Event.GameStatus(status)).broadcast(this);
	}
	private void broadcast(Event event) {
		event.broadcast(this); // WHY?!?! 😭
	}
	public void sendNewConnectionEvents(Player target) {
		// Load all the players
		for (int i = 0; i < this.players.size(); i++) {
			Player p = this.players.get(i);
			// Add this player
			target.fire("addplayer\n" + p.name);
			// Add all the ships
			for (int s = 0; s < p.ships.length; s++) {
				Ship ship = p.ships[s];
				int type = Arrays.asList(ShipType.types).indexOf(ship.type);
				// Add the ship
				target.fire(new Event.AddShip(p, ship, type));
				// Set the maneuver
				if (ship.maneuver != null) {
					target.fire(new Event.SetManeuver(ship));
				}
			}
		}
		// Load the status, as well as any associated information
		(new Event.GameStatus(this.status)).broadcast(this);
		if (this.status == GameStatus.PLANNING) {
			for (int i = 0; i < target.ships.length; i++) {
				if (target.ships[i].maneuver != null) {
					// broadcast(new Event.SetManeuver(target.ships[i]));
				}
			}
	 	} else if (this.status == GameStatus.MOVING) {
			// for (int i = 0; i < this.nextShipId; i++) {
			// 	// ???
			// }
			Ship activeShip = getActiveShip();
			broadcast(new Event.MoveShip(activeShip.id));
		}
	}
	public void startMovingPhase() {
		// Logic to do here:
		// - Update game status to "moving"
		this.setGameStatus(GameStatus.MOVING);
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
		// Optional<Runnable> moveShip = activeShip.maneuver.execute(activeShip);
		// moveShip.ifPresent((v) -> v.run());
		// We do NOT want to move the ship now, that will be done at the end of the turn!
		// Tell everyone we are moving this ship!
		this.broadcast(new Event.MoveShip(activeShip.id));
		// Now wait for the player to send us the action after we've sent the status.
	}
	public void selectActionForActiveShip(Action action) {
		Ship activeShip = this.getActiveShip();
		activeShip.action = action;
		activeShip.action.execute(activeShip); // Execute it!
		// After everyone has pressed Ready
		// this.moveNextShip();
	}
	public Player getPlayerByName(String playerName) {
		List<Player> targets = this.players.stream().filter((x) -> x.name.equals(playerName)).toList();
		Player target = targets.get(0);
		return target;
	}
	private void beginCombatPhase() {
		this.setGameStatus(GameStatus.COMBAT);
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
		this.setGameStatus(GameStatus.PLANNING);
	}
	public void addShip(Player target, int shipIndex) {
		// ship
		ShipType type = ShipType.types[shipIndex];
		Ship ship = new Ship(this, type, new Point(Random.randint(0, 1000), Random.randint(0, 1000)), Random.randint(0, 360), this.nextShipId);
		this.nextShipId++;
		// add the ship
		target.addShip(ship);
		this.ships.add(ship);
		// notify everyone
		this.broadcast(new Event.AddShip(target, ship, shipIndex));
	}
	public static enum GameStatus {
		STARTING,
		PLANNING,
		MOVING,
		COMBAT,
	}
}
