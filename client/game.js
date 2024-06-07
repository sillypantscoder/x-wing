var _map = document.querySelector(".map")
if (_map == null || !(_map instanceof HTMLDivElement)) throw new Error("Map element is missing")
/** @type {HTMLDivElement} */
var map = _map;

/**
 * @param {number} x
 * @param {number} y
 * @param {number} deg
 * @param {number} dist
 */
function movePoint(x, y, deg, dist) {
	var rx = x + (dist * Math.cos(deg * (Math.PI / 180)));
	var ry = y + (dist * Math.sin(deg * (Math.PI / 180)));
	return { x: rx, y: ry };
}
/**
 * @param {string} path
 * @returns {Promise<string>}
 */
function get(path) {
	return new Promise((resolve, reject) => {
		var x = new XMLHttpRequest()
		x.open("GET", path)
		x.addEventListener("loadend", () => {
			if (x.status == 200) resolve(x.responseText)
			else reject(x.status)
		})
		x.send()
	})
}
/**
 * @param {string} path
 * @param {string} body
 * @returns {Promise<void>}
 */
function post(path, body) {
	return new Promise((resolve) => {
		var x = new XMLHttpRequest()
		x.open("POST", path)
		x.addEventListener("loadend", () => {
			resolve()
		})
		x.send(body)
	})
}

class Player {
	/**
	 * @param {string} name
	 * @param {Ship[]} ships
	 */
	constructor(name, ships) {
		/** @type {string} */
		this.name = name
		/** @type {Ship[]} */
		this.ships = ships
	}
}
class ShipType {
	/**
	 * @param {string} shipName
	 * @param {string} pilotName
	 * @param {number} skill
	 * @param {number} attackAmount
	 * @param {number} defendAmount
	 * @param {number} hullValue
	 * @param {number} shieldValue
	 * @param {Maneuver[]} maneuvers
	 * @param {number} size
	 */
	constructor(shipName, pilotName, skill, attackAmount, defendAmount, hullValue, shieldValue, maneuvers, size) {
		/** @type {string} */
		this.shipName = shipName
		/** @type {string} */
		this.pilotName = pilotName
		/** @type {number} */
		this.skill = skill
		/** @type {number} */
		this.attackAmount = attackAmount
		/** @type {number} */
		this.defendAmount = defendAmount
		/** @type {number} */
		this.hullValue = hullValue
		/** @type {number} */
		this.shieldValue = shieldValue
		/** @type {Maneuver[]} */
		this.maneuvers = maneuvers
		/** @type {number} */
		this.size = size
	}
}
class Maneuver {
	/**
	 * @param {number} speed
	 * @param {number} angle
	 * @param {number} stress
	 */
	constructor(speed, angle, stress) {
		/** @type {number} */
		this.speed = speed;
		/** @type {number} */
		this.angle = angle;
		/** @type {number} */
		this.stress = stress;
	}
	/**
	 * @param {Ship} ship
	 */
	execute(ship) {
		// 1. Check if the ship is stressed
		if (ship.isStressed() && this.stress >= 1) return null; // aaaa!
		// 2. Get the new location of the ship
		var newLocation = movePoint(ship.x, ship.y, ship.rot + (this.angle / 2), this.speed * 50);
		var n = {
			cx: newLocation.x + (ship.type.size / 2),
			cy: newLocation.y + (ship.type.size / 2),
			size: ship.type.size,
			angle: ship.rot + this.angle
		}
		for (var i = 0; i < ships.length; i++) {
			var other = ships[i];
			if (other == ship) continue;
			// Get other rect
			var o = {
				cx: other.x + (other.type.size / 2),
				cy: other.y + (other.type.size / 2),
				size: other.type.size,
				angle: other.rot + this.angle
			}
			var collide = rotatedRectanglesCollide(n.cx, n.cy, n.size, n.size, n.angle, o.cx, o.cy, o.size, o.size, o.angle)
			if (collide) return null; // bonk!
		}
		return () => {
			ship.x = newLocation.x;
			ship.y = newLocation.y;
			ship.rot += this.angle;
			ship.stress += this.stress;
			if (ship.stress < 0) ship.stress = 0;
		};
	}
	toString() {
		return "Maneuver { speed: " + this.speed + ", angle: " + this.angle + ", stress: " + this.stress + " }";
	}
	/**
	 * @param {string} set
	 */
	static parseSet(set) {
		/**
		 * @type {Maneuver[]}
		 */
		var maneuvers = [];
		var rows = set.split("\n");
		var speed = 1;
		for (var i = rows.length - 1; i >= 0; i--) {
			// Now go through the columns.
			for (var c = 0; c < rows[i].length; c++) {
				// Check if there is a maneuver here.
				var m = rows[i][c];
				if (m == ' ') continue;
				// Extract the stress value.
				var stress = 0;
				if (m == '+') stress = 1;
				else if (m == '-') stress = -1;
				else if (m == '=') stress = 0;
				else throw new Error("Bad maneuver char '" + m + "' on line " + i + " at position " + c + "!!!!!!!!!!!!!!!");
				// Extract the angle.
				var angle = (c - 2) * 45;
				// Register the maneuver.
				maneuvers.push(new Maneuver(speed, angle, stress));
			}
			// Every row, the speed increases.
			speed += 1;
		}
		return maneuvers
	}
}
class Ship {
	// TODO: Add image for ship

	/**
	 * @param {number} x
	 * @param {number} y
	 * @param {number} rot
	 * @param {ShipType} type
	 */
	constructor(x, y, rot, type) {
		/** @type {ShipType} */
		this.type = type
		// variables
		/** @type {number} */
		this.x = x
		/** @type {number} */
		this.y = y
		/** @type {number} */
		this.rot = rot
		/** @type {number} */
		this.size = 20
		/** @type {number} */
		this.stress = 20
		// register
		ships.push(this)
		// element
		this.elm = document.createElement("div")
		map.appendChild(this.elm)
		this.updateStyle()
		// Click listener
		var ship = this
		// note: the third parameter should be called "early phase"
		this.elm.addEventListener("mousedown", (e) => {
			ship.click()
			e.stopPropagation()
		}, true)
	}
	updateStyle() {
		this.elm.setAttribute("style", `background: orange; --x: ${(this.x * 2) + viewportPos.x}px; --y: ${(this.y * 2) + viewportPos.y}px; --rot: ${this.rot}deg; --size: ${this.size}px;`)
	}
	click() {
		menuShip = this
	}
	closeMenu() {}
	isStressed() {
		return this.stress >= 1
	}
}

/** @type {Ship[]} */
var ships = []
/** @type {Ship | null} */
var menuShip = null

/** @type {{ x: number, y: number }} */
var viewportPos = { x: 0, y: 0 }
/** @type {{ x: number, y: number } | null} */
var dragLoc = null
map.addEventListener("mousedown", (e) => {
	if (menuShip != null) {
		menuShip.closeMenu()
		menuShip = null
	} else {
		dragLoc = { x: e.clientX, y: e.clientY }
	}
})
map.addEventListener("mousemove", (e) => {
	if (dragLoc != null) {
		var move = { x: e.clientX - dragLoc.x, y: e.clientY - dragLoc.y }
		dragLoc = { x: e.clientX, y: e.clientY }
		// move the viewport
		viewportPos.x += move.x
		viewportPos.y += move.y
		map.setAttribute("style", `background-position: ${viewportPos.x}px ${viewportPos.y}px;`)
		for (var i = 0; i < ships.length; i++) {
			ships[i].updateStyle()
		}
	}
})
map.addEventListener("mouseup", (e) => {
	dragLoc = null
})

/**
 * Parse the list of ships into a list of ShipType objects.
 * @param {string} data
 * @returns {ShipType[]}
 */
function parseShipList(data) {
	var ships = data.split("\n\n")
	/** @type {ShipType[]} */
	var parsed = []
	for (var i = 0; i < ships.length; i++) {
		var lines = ships[i].split("\n")
		var shipname = lines[0]
		var pilotname = lines[1]
		var points = lines[2].split(", ").map((x) => x.split(" ")[1]).map((x) => Number(x))
		var maneuvers = Maneuver.parseSet(lines.slice(3, -1).join("\n"))
		var size = Number(lines[lines.length - 1].substring(5))
		parsed.push(new ShipType(shipname, pilotname, points[0], points[1], points[2], points[3], points[4], maneuvers, size))
	}
	return parsed
}
/**
 * @type {ShipType[]}
 */
var ship_types = []
get("ships.txt").then((x) => {
	ship_types = parseShipList(x)
	// make a ship
	var ship = new Ship(100, 100, 0, ship_types[0])
	me.ships.push(ship)
})

var me = new Player("Meeeee", [])