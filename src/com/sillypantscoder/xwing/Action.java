package com.sillypantscoder.xwing;

import java.util.HashMap;

// Things actions need to handle:
//  ☐ Parse selected action and parameters from the UI
//  ☐ Stringify actions associated with ships so the UI can refresh and display
//    selected actions (even after they have taken effect)
//  ☑ Action types (AvailableActions) need to be associated with Ship types
//  x Ships must have lists of configured Actions
//  x Somehow interact with the game engine to ensure that actions are [???]

/**
 * An action that a ship has chosen. This object includes the target ship.
 * The `execute` method is overridden to add what happens when the action executes.
 * The static `create` method (in ActionCreator) is used to load this action
 * 		from its stringified data.
 * The `save` method is overridden to save the actioon back to its stringified data.
 */
public abstract class Action {
	public Ship targetShip;
	public AvailableAction source;
	public Action(Ship targetShip, AvailableAction source) { this.targetShip = targetShip; this.source = source; }
	public abstract void execute();
	public abstract String[] save();
	/**
	 * A function that creates an action from a ship and the action's associated data.
	 * This represents a type of action.
	 */
	@FunctionalInterface
	public static interface ActionCreator {
		public Action create(Ship targetShip, AvailableAction source, String[] data);
	}
	/**
	 * An action that is available to a ship. Includes both the action type,
	 * and whether this action causes stress. This is stored in the ship type.
	 */
	public static class AvailableAction {
		public ActionCreator action;
		public boolean stress;
		public AvailableAction(ActionCreator action, boolean stress) {
			this.action = action;
			this.stress = stress;
		}
		public Action createInstance(Ship targetShip, String[] data) {
			return this.action.create(targetShip, this, data);
		}
	}
	public static ActionCreator getActionForString(String type) {
		HashMap<String, ActionCreator> actionTypes = new HashMap<>();
		actionTypes.put("focus", FocusAction::create);
		actionTypes.put("evade", EvadeAction::create);
		actionTypes.put("barrel-roll", BarrelRollAction::create);
		return actionTypes.get(type);
	}
	public static class FocusAction extends Action {
		public FocusAction(Ship targetShip, AvailableAction source) { super(targetShip, source); }
		public void execute() { targetShip.focused = true; }
		public static FocusAction create(Ship ship, AvailableAction source, String[] data) { return new FocusAction(ship, source); }
		public String[] save() { return new String[] { }; }
	}
	public static class EvadeAction extends Action {
		public EvadeAction(Ship targetShip, AvailableAction source) { super(targetShip, source); }
		public void execute() { targetShip.evading = true; }
		public static EvadeAction create(Ship ship, AvailableAction source, String[] data) { return new EvadeAction(ship, source); }
		public String[] save() { return new String[] { }; }
	}
	public static class BarrelRollAction extends Action {
		public boolean direction; // false for left, true for right
		public BarrelRollAction(Ship targetShip, AvailableAction source, boolean direction) { super(targetShip, source); this.direction = direction; }
		public void execute() {
			targetShip.pos.moveDirection(targetShip.rotation + (direction ? 90 : -90), 40);
		}
		public static BarrelRollAction create(Ship ship, AvailableAction source, String[] data) {
			return new BarrelRollAction(ship, source, data[0] == "right");
		}
		public String[] save() { return new String[] { direction ? "right" : "left" }; }
	}
}

// Server:                                 | Player 1  | Player 2 |
//   Enter movement phase                  |
//   Determine next ship to move: 0        |
//   Move ship 0                           |
//     -- wait for owner to submit action  | barrel-roll-left
//     -- wait for owner to submit action  | barrel-roll-right
//                                      --OR--
//     -- wait for owner to submit action  | focus
//     -- wait for everyone to be ready    |
//                                         | ready     |
//                                         |           | ready

// Sometimes you can't select an action for a ship: True
//
// If you're stressed, you can't select an action
// If you're not stressed, you can always select an action if you want

// Either you can select an action or not.
// If you can, then you may select from any of your actions.
// Some actions require additional input