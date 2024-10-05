package com.sillypantscoder.xwing;

import java.util.List;

import com.sillypantscoder.http.HttpResponse;
import com.sillypantscoder.http.HttpServer;
import com.sillypantscoder.xwing.Game.GameStatus;

/**
 * The main HTTP handler used for the server.
 */
public class MainServer extends HttpServer.RequestHandler {
	public Game game;
	public MainServer() {
		game = new Game();
	}
	public HttpResponse get(String path) {
		if (path.equals("/")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/html").setBody(Utils.readFile("client/login.html"));
		if (path.startsWith("/game.html?")) {
			String user = path.substring(11);
			List<Player> targets = game.players.stream().filter((x) -> x.name.equals(user)).toList();
			if (targets.size() == 0) {
				return new HttpResponse().setStatus(302).addHeader("Location", "/");
			}
			Player target = targets.get(0);
			target.resetEvents();
			return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/html").setBody(Utils.readFile("client/game.html"));
		}
		if (path.equals("/game.js")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/javascript").setBody(Utils.readFile("client/game.js"));
		if (path.equals("/collide.js")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/javascript").setBody(Utils.readFile("client/collide.js"));
		if (path.equals("/ships.txt")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/plain").setBody(Utils.readFile("ships.txt"));
		if (path.startsWith("/events/")) {
			String user = path.substring(8);
			List<Player> targets = game.players.stream().filter((x) -> x.name.equals(user)).toList();
			if (targets.size() == 0) {
				return new HttpResponse().setStatus(404).setBody("You are not logged in");
			}
			Player target = targets.get(0);
			return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/plain").setBody(target.getEvents());
		}
		System.err.println("Error for GET request: " + path);
		return new HttpResponse().setStatus(404).setBody("unknown path");
	}
	public HttpResponse post(String path, String body) {
		if (path.equals("/join")) {
			if (game.status != GameStatus.STARTING) {
				return new HttpResponse().setStatus(400).setBody("wrong state");
			}
			String playerName = body.split("\n")[0];
			String teamName = body.split("\n")[1];
			if (playerName.matches("[A-Za-z0-9 ]+")) {
				// TODO: cannot use same name as another player
				game.login(playerName, teamName);
				return new HttpResponse().setStatus(200).setBody("it worked!");
			} else {
				return new HttpResponse().setStatus(400).setBody("invalid name");
			}
		}
		if (path.equals("/ready")) {
			String playerName = body;
			if (game.status == GameStatus.STARTING ||
					game.status == GameStatus.MOVING ||
					game.status == GameStatus.COMBAT) {
				game.markReady(playerName);
				return new HttpResponse().setStatus(200);
			}
		}
		if (path.equals("/place_ship")) {
			// TODO: correct phase only
			String[] bodyLines = body.split("\n");
			if (bodyLines.length != 4) {
				return new HttpResponse().setStatus(400).setBody("expected 4 lines");
			}
			int shipID = Integer.parseInt(bodyLines[0]);
			int newX = Integer.parseInt(bodyLines[1]);
			int newY = Integer.parseInt(bodyLines[2]);
			int newRot = Integer.parseInt(bodyLines[3]);
			try {
				game.placeShip(shipID, new Point(newX, newY), newRot);
			} catch (Error e) {
				return new HttpResponse().setStatus(400).setBody(e.getMessage());
			}
			return new HttpResponse().setStatus(200);
		}
		if (path.equals("/add_ship")) {
			// TODO: correct phase only
			String[] bodyLines = body.split("\n");
			if (bodyLines.length != 2) {
				return new HttpResponse().setStatus(400).setBody("expected 2 lines");
			}
			String playerName = bodyLines[0];
			Player target = game.getPlayerByName(playerName);
			int shipIndex = Integer.parseInt(bodyLines[1]);
			if (game.status == GameStatus.STARTING) {
				game.addShip(target, shipIndex);
				return new HttpResponse().setStatus(200);
			}
			return new HttpResponse().setStatus(400).setBody("game status is not starting (" + game.status.name() + ")");
		}
		// if (path.equals("/set_action")) {
		// 	String[] bodyLines = body.split("\n");
		// 	int actionIndex = Integer.parseInt(bodyLines[0]);
		// 	String[] actionData = Arrays.copyOfRange(bodyLines, 1, bodyLines.length);
		// 	if (game.status == GameStatus.WAITINGFORACTION) {
		// 		game.assignActionForActiveShip(actionIndex, actionData);
		// 		return new HttpResponse().setStatus(200);
		// 	}
		// 	return new HttpResponse().setStatus(400).setBody("game status is not moving (" + game.status.name() + ")");
		// }
		if (path.equals("/maneuvers")) {
			// Submitting the maneuvers during the planning phase
			if (game.status == GameStatus.PLANNING) {
				String[] bodyLines = body.split("\n");
				String playerName = bodyLines[0];
				Player target = game.getPlayerByName(playerName);
				target.setReady(true);
				// Go through all the lines and set the maneuvers
				for (int i = 1; i < bodyLines.length; i++) {
					String[] mdata = bodyLines[i].split(" ");
					int shipID = Integer.parseInt(mdata[0]);
					int maneuverID = Integer.parseInt(mdata[1]);
					Ship ship = target.getShipByID(shipID);
					Maneuver maneuver = ship.type.maneuvers[maneuverID];
					ship.maneuver = maneuver;
				}
				// Go to the next phase if we are all done
				for (int i = 0; i < game.ships.size(); i++) {
					if (game.ships.get(i).maneuver == null) {
						return new HttpResponse().setStatus(200);
					}
				}
				// We are all done! Go to the next phase
				game.startMovingPhase();
				return new HttpResponse().setStatus(200);
			}
			return new HttpResponse().setStatus(400).setBody("game status is not planning (" + game.status.name() + ")");
		}
		System.err.println("Error for POST request: " + path);
		return new HttpResponse().setStatus(404).setBody("unknown path");
	}
}
