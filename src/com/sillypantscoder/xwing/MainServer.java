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
			Player target = targets.get(0);
			return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/plain").setBody(target.getEvents());
		}
		System.err.println("Error for request: " + path);
		return new HttpResponse().setStatus(404);
	}
	public HttpResponse post(String path, String body) {
		if (path.equals("/add_ship")) {
			if (game.status == GameStatus.PLANNING) {
				String user = body.split("\n")[0];
				List<Player> targets = game.players.stream().filter((x) -> x.name.equals(user)).toList();
				Player target = targets.get(0);
				// ship
				int index = Integer.parseInt(body.split("\n")[1]);
				ShipType type = ShipType.getTypes()[index];
				Ship s = new Ship(game, type, new Point(Random.randint(0, 1000), Random.randint(0, 1000)), Random.randint(0, 360));
				// add the ship
				target.addShip(s);
				game.ships.add(s);
				// notify everyone
				for (int i = 0; i < game.players.size(); i++) {
					game.players.get(i).fire("addship\n" + user + "\n" + index + "\n" + s.pos.x + "\n" + s.pos.y + "\n" + s.rotation);
				}
				// finish
				return new HttpResponse().setStatus(200);
			}
			return new HttpResponse().setStatus(400);
		}
		System.err.println("Error for POST request: " + path);
		return new HttpResponse().setStatus(404);
	}
}
