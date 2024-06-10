package com.sillypantscoder.xwing;

public abstract class Event {
	public Event() {}
	public abstract String[] getSendString();
	public void fire(Player player) {
		player.fire(String.join("\n", this.getSendString()));
	}
	public void broadcast(Game game) {
		for (int i = 0; i < game.players.size(); i++) {
			this.fire(game.players.get(i));
		}
	}
	// private void broadcast(Game game) {
	// 	game.broadcast(this);
	// }
	public static class PlayerLogin extends Event {
		private Player newPlayer;
		public PlayerLogin(Player newPlayer) { this.newPlayer = newPlayer; }
		public String[] getSendString() {
			return new String[] { "addplayer", this.newPlayer.name };
		}
	}
	public static class GameStatus extends Event {
		private Game.GameStatus status;
		public GameStatus(Game.GameStatus status) { this.status = status; }
		public String[] getSendString() {
			return new String[] { "status", this.status.toString() };
		}
	}
	public static class SetManeuver extends Event {
		private Ship target;
		public SetManeuver(Ship target) { this.target = target; }
		public String[] getSendString() {
			return new String[] { "setmaneuver", "" + target.id, target.pleaseGiveMeTheManeuverIndexAsAStringSoIDontMakeMyEyesBleedWithTheAbsoluteAbominationOfDoingItElsewhere() };
		}
	}
	public static class AddShip extends Event {
		private Player target;
		private Ship ship;
		private int typeIndex;
		public AddShip(Player target, Ship ship, int index) { 
			this.target = target; this.ship = ship; this.typeIndex = index;
		}
		// this.broadcast("addship\n" + target.name + "\n" + shipIndex + "\n" + s.pos.x + "\n" + s.pos.y + "\n" + s.rotation + "\n" + s.id);
		public String[] getSendString() {
			return new String[] {
				"addship",
				this.target.name,
				// :D
				("horriblestringconversion" + this.typeIndex).substring(24),
				("terriblestringconversion" + this.ship.pos.x).substring(24),
				("horriblestringconversion" + this.ship.pos.y).substring(24),
				("terriblestringconversion" + this.ship.rotation).substring(24),
				("evenworsestringconversion" + this.ship.id).substring(25),
			};
		}
	}
	public static class MoveShip extends Event {
		private int shipId;
		public MoveShip(int shipId) { this.shipId = shipId; }
		public String[] getSendString() {
			return new String[] { "moveship", "" + this.shipId };
		}
	}
}