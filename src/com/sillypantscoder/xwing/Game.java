package com.sillypantscoder.xwing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sillypantscoder.xwing.Action.AvailableAction;

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
		this.login("jason", Team.teams[0].name);
	}
	public static Team getTeamByName(String name) {
		for (int i = 0; i < Team.teams.length; i++) {
			if (Team.teams[i].name.equals(name)) {
				return Team.teams[i];
			}
		}
		throw new Error("Team with name: " + name + " not found!");
	}
	public void login(String name, String teamName) {
		// ShipType[] types = ShipType.getTypes();
		// String[] indexes = ships.split("\n");
		// Ship[] parsed = new Ship[indexes.length];
		// for (int i = 0; i < indexes.length; i++) {
		// 	int type = Integer.parseInt(indexes[i]);
		// 	Ship newShip = new Ship(this, types[type], new Point(0, 0), 0);
		// 	parsed[i] = newShip;
		// 	this.ships.add(newShip);
		// }
		Team team = Game.getTeamByName(teamName);
		Player newPlayer = new Player(this, name, team, new Ship[] {});
		this.players.add(newPlayer);
		// Notify everyone
		(new Event.PlayerLogin(newPlayer)).broadcast(this);
	}
	public void addShip(Player target, int shipIndex) {
		// ship
		ShipType type = ShipType.types[shipIndex];
		Ship ship = new Ship(this, type, new Point(Random.randint(2, 10) * 20, Random.randint(2, 10) * 20), Random.choice(new Integer[] { 0, 45, 90, 90+45, 180, 180+45, 180+90, 180+90+45 }), this.nextShipId);
		this.nextShipId++;
		// add the ship
		target.addShip(ship);
		this.ships.add(ship);
		// notify everyone
		this.broadcast(new Event.AddShip(target, ship, shipIndex));
	}
	private void setGameStatus(GameStatus status) {
		this.unreadyAllPlayers();
		// Set status
		this.status = status;
		(new Event.GameStatus(status)).broadcast(this);
	}
	public void broadcast(Event event) {
		event.broadcast(this); // WHY?!?! 😭
	}
	public void sendNewConnectionEvents(Player target) {
		// Load all the players
		for (int i = 0; i < this.players.size(); i++) {
			Player p = this.players.get(i);
			// Add this player
			target.fire(new Event.PlayerLogin(p));
			// Add all the ships
			for (int s = 0; s < p.ships.length; s++) {
				Ship ship = p.ships[s];
				int type = Arrays.asList(ShipType.types).indexOf(ship.type);
				// Add the ship
				target.fire(new Event.AddShip(p, ship, type));
				// Set the maneuver, but only if we own this ship!!!
				if (Arrays.asList(target.ships).contains(ship) && ship.maneuver != null) {
					target.fire(new Event.SetManeuver(ship));
				}
			}
			if (p.ready) target.fire(new Event.PlayerReady(p, true));
		}
		// Load the status, as well as any associated information
		target.fire(new Event.GameStatus(this.status));
		if (this.status == GameStatus.MOVING) {
			Ship activeShip = getActiveShip();
			target.fire(new Event.SetManeuver(activeShip));
			target.fire(new Event.MoveShip(activeShip.id));
			if (activeShip.action != null) {
				target.fire(new Event.SetAction(activeShip));
			}
		}
	}
	private Ship getShipByID(int shipID) {
		for (int i = 0; i < this.ships.size(); i++) {
			if (this.ships.get(i).id == shipID) {
				return this.ships.get(i);
			}
		}
		throw new Error("ship with id: " + shipID + " not found");
	}
	public void placeShip(int shipID, Point newPos, int newRot) {
		if (this.status != GameStatus.STARTING) {
			throw new Error("Must be in starting phase to move or place ships");
		}
		Ship ship = this.getShipByID(shipID);
		ship.pos = newPos;
		ship.rotation = newRot;
		this.broadcast(new Event.PlaceShip(shipID, newPos, newRot));
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
	private void unreadyAllPlayers() {
		// reset ready for all players
		for (int i = 0; i < this.players.size(); i++) {
			this.players.get(i).setReady(false);
		}
	}
	private void moveNextShip() {
		this.unreadyAllPlayers();
		// Activate the next ship
		Ship activeShip = this.activateNextShip();
		if (activeShip == null) {
			this.beginCombatPhase(); // :O
			return;
		}
		// We do NOT want to move the ship now, that will be done at the end of the turn!
		// Tell everyone we are moving this ship!
		this.broadcast(new Event.SetManeuver(activeShip));
		this.broadcast(new Event.MoveShip(activeShip.id));
		// Now wait for the player to send us the action after we've sent the status.
	}
	public void assignActionForActiveShip(int actionIndexInShipType, String[] actionData) {
		Ship activeShip = this.getActiveShip();
		AvailableAction availableAction = activeShip.type.actions[actionIndexInShipType];
		Action action = availableAction.createInstance(activeShip, actionData);
		activeShip.action = action;
		// After everyone has pressed Ready (todo)
		// activeShip.action.execute(); // Execute it!
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
		target.setReady(true);
		// Are we all ready?
		boolean allready = true;
		for (int i = 0; i < this.players.size(); i++) {
			if (this.players.get(i).ready == false) allready = false;
		}
		if (allready) {
			// start the next phase, whichever that is.
			if (this.status == GameStatus.STARTING) this.startPlanningPhase();
			else if (this.status == GameStatus.MOVING) this.finishMovingTurn();
			else if (this.status == GameStatus.COMBAT) this.startPlanningPhase();
			else {
				System.out.println("state is incorrect: player '" + playerName + "' marked ready (and all players are ready) when server status is " + this.status.name());
			}
		}
	}
	public void finishMovingTurn() {
		// Move the ship
		Ship activeShip = getActiveShip();
		Maneuver.Result moveShip = activeShip.maneuver.compute(activeShip);
		if (moveShip.failed()) {
			// throw new Error("ERROR! Ship with id " + activeShip.id + " cannot be moved");
		} else {
			moveShip.apply(activeShip);
		}
		activeShip.maneuver = null;

		if (activeShip.action != null) {
			// TODO: what if the action fails?
			activeShip.action.execute();
			activeShip.action = null;
		}

		// Inform the clients
		this.broadcast(new Event.ShipDoneMoving(activeShip.id));
		// Continue
		this.moveNextShip();
	}
	public void startPlanningPhase() {
		this.setGameStatus(GameStatus.PLANNING);
	}
	public static enum GameStatus {
		STARTING,
		PLANNING,
		MOVING,
		COMBAT,
	}
}
