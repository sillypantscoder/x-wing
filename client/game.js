// --------------------------------- GLOBALS ---------------------------------

const GRIDSIZE = 50;

var _map = document.querySelector(".map")
if (_map == null || !(_map instanceof HTMLDivElement)) throw new Error("Map element is missing")
/** @type {HTMLDivElement} */
var map = _map;

var _status = document.querySelector(".status")
if (_status == null || !(_status instanceof HTMLDivElement)) throw new Error("Status element is missing")
/** @type {HTMLDivElement} */
var statusBar = _status;

var _players = document.querySelector(".players")
if (_players == null || !(_players instanceof HTMLDivElement)) throw new Error("Player bar element is missing")
/** @type {HTMLDivElement} */
var playerBar = _players;

/** @type {"starting" | "planning" | "moving" | "combat"} */
var gamePhase = "starting"
/** @type {Player[]} */
var players = []

/** @type {Ship[]} */
var ships = []
/** @type {Menu | null} */
var activeMenu = null

/** @type {{ x: number, y: number }} */
var viewportPos = { x: 0, y: 0 }
/** @type {{ x: number, y: number } | null} */
var dragLoc = null

/**
 * @type {ShipType[]}
 */
var ship_types = []
/**
 * @type {Team[]}
 */
var teams = []

var my_name = location.search.substring(1)
/**
 * @type {Player | null}
 */
var me = null

// ------------------------------- END GLOBALS -------------------------------

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
 * @param {{ x: number,  y: number }} pos
 */
function formatPos(pos) {
	return `${(pos.x * 2) + viewportPos.x} ${(pos.y * 2) + viewportPos.y}`
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

class Team {
	/**
	 * @param {string} name
	 * @param {string[]} colors
	 */
	constructor(name, colors) {
		/** @type {string} */
		this.name = name
		/** @type {string[]} */
		this.colors = colors
	}
}
class Player {
	/**
	 * @param {string} name
	 * @param {Team} team
	 * @param {Ship[]} ships
	 */
	constructor(name, team, ships) {
		/** @type {string} */
		this.name = name
		/** @type {Team} */
		this.team = team
		/** @type {Ship[]} */
		this.ships = ships
		/** @type {boolean} */
		this.ready = false
	}
}
class ShipType {
	/**
	 * @param {Team} team
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
	constructor(team, shipName, pilotName, skill, attackAmount, defendAmount, hullValue, shieldValue, maneuvers, size) {
		/** @type {Team} */
		this.team = team
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
	compute(ship) {
		// Get the new location of the ship
		//  The angle is divided by two because the angle defines how much the
		//  ship turns, but we're computing the destination position. If you turn
		//  90° then you are moving along a circular arc and will end up at half
		//  that angle.... maybe.
		//  Yes, that's totally right.
		let newLocation = movePoint(ship.x, ship.y, ship.rot + (this.angle / 2), this.speed * GRIDSIZE);
		let n = {
			cx: newLocation.x + (ship.type.size / 2),
			cy: newLocation.y + (ship.type.size / 2),
			size: ship.type.size,
			angle: ship.rot + this.angle
		}
		return {
			x: newLocation.x,
			y: newLocation.y,
			angle: ship.rot + this.angle,
			isInvalid: this.checkForCollisions(ship, n) || (ship.isStressed() && this.stress >= 1),
			exec: () => {
				ship.x = newLocation.x;
				ship.y = newLocation.y;
				ship.rot += this.angle;
				ship.stress += this.stress;
				if (ship.stress < 0) ship.stress = 0;
			}
		}
	}
	/**
	 * @param {Ship} ship
	 * @param {{ cx: number, cy: number, size: number, angle:number }} checkRect
	 */
	checkForCollisions(ship, checkRect) {
		const n = checkRect;
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
			if (collide) return true // bonk!
		}
		return false
	}
	/**
	 * @param {Ship} ship
	 * @param {string} color
	 */
	createPreview(ship, color) {
		var data = this.compute(ship)
		var _speed = this.speed
		var originalPos = { x: ship.x, y: ship.y, r: ship.rot }
		// Preview square
		var preview_square = document.createElement("div")
		preview_square.classList.add("preview")
		map.appendChild(preview_square)
		function updateSquareStyle() {
			var styles = `--x: ${(data.x * 2) + viewportPos.x}px; --y: ${(data.y * 2) + viewportPos.y}px; --size: ${ship.type.size}px; --rot: ${data.angle}deg; box-shadow: 0 0 5px 5px ${color};`
			preview_square.setAttribute("style", styles);
		}
		updateSquareStyle()
		preview_square.addEventListener("updatestyle", updateSquareStyle)
		// Preview line
		var preview_line = document.createElementNS("http://www.w3.org/2000/svg", "svg")
		map.appendChild(preview_line)
		preview_line.setAttribute("style", `overflow: visible;`)
		preview_line.classList.add("preview")
		function updateLineStyle() {
			var shiptarget = movePoint(data.x, data.y, data.angle + 180, ship.size * 0.5 * 0.8)
			var mid_pos = movePoint(originalPos.x, originalPos.y, originalPos.r, _speed * 30)
			preview_line.innerHTML = `<path d="M ${formatPos(originalPos)} Q ${formatPos(mid_pos)} ${formatPos(shiptarget)}" fill="none" stroke-width="3" stroke="${color}" style="transform: translate(6px, 6px); opacity: 0.5;" />`;
		}
		updateLineStyle()
		preview_line.addEventListener("updatestyle", updateLineStyle)
		// Finish
		return [preview_square, preview_line]
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
	 * @param {number} id
	 */
	constructor(x, y, rot, type, id) {
		var ship = this
		/** @type {number} */
		this.id = id
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
		this.stress = 0
		// planning
		/** @type {Maneuver | null} */
		this.maneuver = null
		/** @type {Element[]} */
		this.previewElements = []
		// register
		ships.push(this)
		// element
		this.elm = document.createElement("div")
		map.appendChild(this.elm)
		this.elm.addEventListener("updatestyle", () => ship.updateStyle())
		// @ts-ignore
		this.elm._SourceShip = this // for debugging
		/** @type {Menu | null} */
		this.menu = null
		// circle element
		this.circle = document.createElement("div")
		map.appendChild(this.circle)
		this.circle.classList.add("ship-circle")
		this.updateStyle()
		// Click listener
		// note: the third parameter should be called "early phase"
		this.elm.addEventListener("mousedown", (e) => {
			if (activeMenu != null) {
				activeMenu.closeMenu()
				activeMenu = null
			}
			ship.click()
			e.stopPropagation()
		}, true)
	}
	ownedByMe() {
		if (me == null) return false
		return me.ships.includes(this)
	}
	updateStyle() {
		if (this.previewElements.length == 0 && this.maneuver != null) {
			this.previewElements = this.maneuver.createPreview(this, "#0FF5")
		}
		this.elm.setAttribute("style", `background: linear-gradient(90deg, orange 80%, black); --x: ${(this.x * 2) + viewportPos.x}px; --y: ${(this.y * 2) + viewportPos.y}px; --rot: ${this.rot}deg; --size: ${this.size}px;`)
		if (this.emphasized()) {
			this.elm.classList.add("em")
		} else {
			this.elm.classList.remove("em")
		}
		this.circle.setAttribute("style", `background-color: ${this.ownedByMe() ? "green" : "red"}; --x: ${(this.x * 2) + viewportPos.x}px; --y: ${(this.y * 2) + viewportPos.y}px; --size: ${this.size * 2}px;`)
	}
	emphasized() {
		if (gamePhase == "planning") return (this.maneuver == null) && this.ownedByMe()
		return false
	}
	click() {
		if (gamePhase == "starting" && this.ownedByMe()) {
			this.menu = new MoveShipMenu(this)
		} else if (gamePhase == "planning" && this.ownedByMe()) {
			this.menu = new ManeuverMenu(this)
		} else {
			this.menu = new BlankMenu(this)
		}
	}
	isStressed() {
		return this.stress >= 1
	}
	removePreviews() {
		this.previewElements.forEach((v) => v.remove())
		this.previewElements = []
	}
	sendMovement() {
		post("/place_ship", `${this.id}\n${this.x}\n${this.y}\n${this.rot}`)
	}
}
class Menu {
	/**
	 * @param {Ship} ship
	 */
	constructor(ship) {
		/** @type {Ship} */
		this.ship = ship
		activeMenu = this
		// Elements
		/*
			elm				Has a size of 0x0 and is used for positioning.
			 |- mainDialog	The actual dialog box.
			     |- tokens	The list of tokens, if needed.
				 |- main	The actual content of the menu.
		 */
		// elm
		this.elm = document.createElement("div")
		this.elm.classList.add("menu")
		this.ship.elm.appendChild(this.elm)
		// mainDialog
		this.mainDialog = document.createElement("div")
		this.elm.appendChild(this.mainDialog)
		// tokens
		this.tokens = document.createElement("div")
		this.mainDialog.appendChild(this.tokens)
		this.tokens.classList.add("tokens")
		this.populateTokens()
		// main
		this.main = document.createElement("div")
		this.mainDialog.appendChild(this.main)
	}
	populateTokens() {
		for (var i = 0; i < this.ship.stress; i++) {
			this.tokens.innerHTML += `<span class="token-hover-stress"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><rect x="0" y="0" width="20" height="20" rx="4" fill="red" /><path d="M 8 2 L 12 2 L 12 13 L 8 13 Z M 8 15 L 12 15 L 12 18 L 8 18 Z" fill="white" /></svg></span>`
		}
	}
	/**
	 * @param {number} x
	 * @param {number} y
	 */
	closeMenuWithLocation(x, y) {
		this.closeMenu()
	}
	closeMenu() {
		this.ship.menu = null
		this.elm.remove()
	}
}
class BlankMenu extends Menu {
	/**
	 * @param {Ship} ship
	 */
	constructor(ship) {
		super(ship)
		this.main.setAttribute("style", `min-width: 20ch;`)
		this.main.innerText = "No actions are available for this ship."
	}
}
class MoveShipMenu extends Menu {
	/**
	 * @param {Ship} ship
	 */
	constructor(ship) {
		super(ship)
		var _menu = this
		this.moving = false
		this.main.innerHTML = `<button>Move ship</button><button>Rotate</button>`
		this.main.children[0].addEventListener("mouseup", () => {
			_menu.moving = true
			_menu.main.innerHTML = `Click somewhere to place the ship`
		})
		this.main.children[1].addEventListener("mouseup", () => {
			ship.rot = Math.round((ship.rot / 45) + 1) * 45
			this.ship.sendMovement()
			ship.updateStyle()
		})
	}
	/**
	 * @param {number} x
	 * @param {number} y
	 */
	closeMenuWithLocation(x, y) {
		if (this.moving) {
			this.ship.x = Math.round(x / 20) * 20
			this.ship.y = Math.round(y / 20) * 20
			this.ship.sendMovement()
			this.ship.updateStyle()
		}
		this.closeMenu()
	}
}
class ManeuverMenu extends Menu {
	/**
	 * @param {Ship} ship
	 */
	constructor(ship) {
		super(ship)
		/** @type {(HTMLDivElement | SVGSVGElement)[]} */
		this.previewElements = []
		this.main.setAttribute("style", `width: max-content;`) // transform: scale(2); transform-origin: center top;
		var maxSpeed = Math.max(...ship.type.maneuvers.map((v) => v.speed))
		this.main.innerHTML = `<table style="font-size: 1.5em;"></table>`
		var table = this.main.children[0]
		for (var speed = maxSpeed; speed >= 1; speed--) {
			var row = document.createElement("tr")
			table.appendChild(row)
			row.appendChild(document.createElement("th")).innerText = `${speed}`
			for (var angle = 0; angle < 5; angle++) {
				// Make the element
				var e = document.createElement("td")
				row.appendChild(e)
				// Find the maneuver here, if it exists
				var maneuver = ship.type.maneuvers.find((v) => (v.speed == speed && v.angle == (angle - 2) * 45))
				if (maneuver == undefined) continue;
				// Make the arrow
				var arrowPos = movePoint(5, 4, maneuver.angle - 90, 2)
				var color = ["blue", "black", "red"][maneuver.stress + 1]
				e.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10"><path d="M 5 9 Q 5 4 ${arrowPos.x} ${arrowPos.y}" stroke="${color}" stroke-width="2" fill="none" /><path d="M 2.5 2 L 5 0 L 7.5 2 Z" style="transform: rotate(${maneuver.angle}deg); transform-origin: 5px 4px;" fill="${color}" /></svg>`;
				// Clicky
				((_menu, maneuver) => {
					e.addEventListener("mouseup", () => {
						// Remove old elements
						_menu.ship.previewElements.forEach((e) => e.remove())
						// Register the maneuver
						_menu.ship.maneuver = maneuver
						// Make the preview
						_menu.ship.previewElements = maneuver.createPreview(ship, "#0FF5")
						// Update the ship
						_menu.ship.updateStyle()
						// Finish
						_menu.closeMenu()
						setStatusBar()
					}, true)
				})(this, maneuver);
				// make preview thingy!
				var previews = maneuver.createPreview(ship, "orange");
				this.previewElements.push(...previews)
				var styles = previews[0].getAttribute("style") ?? "";
				// Mousey
				((styles, preview) => {
					e.addEventListener("mouseover", () => {
						preview[0].setAttribute("style", styles.replace("orange", "red"))
						preview[1].children[0].setAttribute("stroke", "red")
					}, true)
					e.addEventListener("mouseout", () => {
						preview[0].setAttribute("style", styles)
						preview[1].children[0].setAttribute("stroke", "orange")
					}, true)
				})(styles, previews);
			}
		}
	}
	closeMenu() {
		super.closeMenu()
		for (var i = 0; i < this.previewElements.length; i++) {
			this.previewElements[i].remove();
		}
	}
}


/**
 * Parse the list of ships into a list of ShipType objects.
 * @param {string} data
 * @returns {ShipType[]}
 */
function parseShipList(data) {
	var ships = data.split("\n\n")
	/** @type {Team | null} */
	var currentTeam = null
	/** @type {ShipType[]} */
	var parsed = []
	for (var i = 0; i < ships.length; i++) {
		var lines = ships[i].split("\n")
		if (lines[0].startsWith("TEAM")) {
			// Team entry
			var teamname = lines[0].substring(5);
			var colors = lines[1].substring(8).split(" ");
			currentTeam = new Team(teamname, colors);
			teams.push(currentTeam)
		} else {
			// Regular ship entry
			var shipname = lines[0]
			var pilotname = lines[1]
			var points = lines[2].split(", ").map((x) => x.split(" ")[1]).map((x) => Number(x))
			var maneuvers = Maneuver.parseSet(lines.slice(3, -1).join("\n"))
			var size = Number(lines[lines.length - 1].substring(5))
			if (currentTeam == null) throw new Error("Something is wrong with ships.txt and it's probably also causing an error on the server so go look over there")
			parsed.push(new ShipType(currentTeam, shipname, pilotname, points[0], points[1], points[2], points[3], points[4], maneuvers, size))
		}
	}
	return parsed
}

function setStatusBar() {
	[...statusBar.children].forEach((e) => e.remove())
	var indicator = document.createElement("div")
	statusBar.appendChild(indicator)
	indicator.classList.add("statusindicator")
	indicator.innerText = gamePhase[0].toUpperCase() + gamePhase.substring(1) + " Phase"
	if (gamePhase == "starting") {
		// add ships
		var mainShips = document.createElement("div")
		statusBar.appendChild(mainShips)
		mainShips.appendChild(document.createElement("h1")).innerText = "Add ships:"
		for (var i = 0; i < ship_types.length; i++) {
			var type = ship_types[i];
			if (me == null || type.team != me.team) continue;
			var e = document.createElement("button")
			mainShips.appendChild(e)
			e.innerText = type.shipName + " (" + type.pilotName + ")";
			((i) => { e.addEventListener("click", () => {
				post("/add_ship", my_name + "\n" + i)
			}) })(i);
		}
		// add ready button
		var mainReady = document.createElement("div")
		statusBar.appendChild(mainReady)
		var e = document.createElement("button")
		mainReady.appendChild(e)
		e.innerText = "Ready!"
		// click
		e.addEventListener("click", () => {
			post("/ready", my_name);
			[...statusBar.children].forEach((e) => e.remove())
		})
	} else if (gamePhase == "planning") {
		// add ready button
		var mainReady = document.createElement("div")
		statusBar.appendChild(mainReady)
		var e = document.createElement("button")
		mainReady.appendChild(e)
		e.innerText = "Submit!"
		// disabled?
		var disabled = false
		if (me == null) disabled = true
		else for (var i = 0; i < me.ships.length; i++) {
			if (me.ships[i].maneuver == null) disabled = true
		}
		if (disabled) e.setAttribute("disabled", "true")
		// click
		e.addEventListener("click", () => {
			submitManeuvers()
			e.remove()
		})
	} else if (gamePhase == "moving") {
		// add ready button
		var mainReady = document.createElement("div")
		statusBar.appendChild(mainReady)
		var e = document.createElement("button")
		mainReady.appendChild(e)
		e.innerText = "Ready!"
		// click
		e.addEventListener("click", () => {
			post("/ready", my_name)
			e.disabled = true
		})
	} else if (gamePhase == "combat") {
		// add ready button
		var mainReady = document.createElement("div")
		statusBar.appendChild(mainReady)
		mainReady.appendChild(document.createElement("h1")).innerText = "This part has not been coded yet"
		mainReady.appendChild(document.createElement("h1")).innerText = "Sorry :("
		var e = document.createElement("button")
		mainReady.appendChild(e)
		e.innerText = "Ready!"
		// click
		e.addEventListener("click", () => {
			post("/ready", my_name)
			e.remove()
		})
	}
}
function updatePlayerBar() {
	[...playerBar.children].forEach((e) => e.remove())
	for (var i = 0; i < players.length; i++) {
		var e = document.createElement("div")
		playerBar.appendChild(e)
		e.innerHTML = `<div><b></b></div><div></div><div></div><div style="height: 1em;"></div>`
		e.children[0].children[0].textContent = players[i].name
		e.children[1].textContent = `Team: ${players[i].team.name}`
		e.children[2].textContent = `${players[i].ships.length} ship${players[i].ships.length==1 ? '' : 's'}`
		if (players[i] == me) e.classList.add("player-me")
		if (players[i].ready) e.children[3].appendChild(document.createElement("span")).classList.add("ready")
	}
}

/**
 * @param {string} data
 */
function handleEventResponse(data) {
	var items = data.split("\n\n").map((x) => x.split("\n")).filter((v) => v.length != 1 || v[0] != "");
	for (var i = 0; i < items.length; i++) {
		if (items[i][0] == "addplayer") {
			var newPlayer = new Player(items[i][1], getTeamByName(items[i][2]), [])
			players.push(newPlayer)
			if (newPlayer.name == my_name && me == null) {
				me = newPlayer
			}
			updatePlayerBar()
		} else if (items[i][0] == "addship") {
			var target = players.find((v) => v.name == items[i][1])
			if (target == null) throw new Error("Player with name '" + items[i][1] + "' not found!")
			var type = ship_types[Number(items[i][2])]
			var newShip = new Ship(Number(items[i][3]), Number(items[i][4]), Number(items[i][5]), type, Number(items[i][6]))
			target.ships.push(newShip)
			newShip.updateStyle()
			updatePlayerBar()
		} else if (items[i][0] == "placeship") {
			var id = Number(items[i][1])
			var ship = getShipFromID(id)
			ship.x = Number(items[i][2])
			ship.y = Number(items[i][3])
			ship.rot = Number(items[i][4])
			ship.updateStyle()
		} else if (items[i][0] == "status") {
			/** @type {Object.<string, "starting" | "planning" | "moving" | "combat">} */
			var m = {
				"STARTING": "starting",
				"PLANNING": "planning",
				"MOVING": "moving",
				"COMBAT": "combat"
			}
			/** @type {"starting" | "planning" | "moving" | "combat"} */
			var newStatus = m[items[i][1]]
			gamePhase = newStatus;
			setStatusBar()
			for (var j = 0; j < ships.length; j++) {
				ships[j].updateStyle()
			}
		} else if (items[i][0] == "setmaneuver") {
			var shipID = Number(items[i][1])
			var maneuverID = Number(items[i][2])
			var ship = getShipFromID(shipID)
			var maneuver = ship.type.maneuvers[maneuverID]
			ship.maneuver = maneuver
			// update
			ship.updateStyle()
			setStatusBar()
		} else if (items[i][0] == "moveship") {
			var shipID = Number(items[i][1])
			var ship = getShipFromID(shipID)
			if (ship.maneuver != null) {
				// Reset the previews
				ship.removePreviews()
				// Execute the maneuver
				var result = ship.maneuver.compute(ship)
				if (result.isInvalid) {
					ship.previewElements = ship.maneuver.createPreview(ship, "red")
				} else {
					ship.previewElements = ship.maneuver.createPreview(ship, "lime")
					result.exec()
				}
				ship.maneuver = null
				// Update
				ship.updateStyle()
				setStatusBar()
			} else {
				console.error("Error:", ship)
				throw new Error("Cannot move ship with ID " + shipID + " because its maneuver is not set")
			}
		} else if (items[i][0] == "ready") {
			var target = players.find((v) => v.name == items[i][1])
			if (target == null) throw new Error("Player with name '" + items[i][1] + "' not found!")
			target.ready = items[i][2] == "true"
			updatePlayerBar()
		} else if (items[i][0] == "shipdonemoving") {
			var shipID = Number(items[i][1])
			var ship = getShipFromID(shipID)
			ship.removePreviews()
		} else {
			console.error("Unknown event was recieved!!!", items[i])
		}
	}
}
async function eventCheckerLoop() {
	while (true) {
		await get("/events/" + my_name).then(handleEventResponse).catch((e) => {
			// alert("Disconnected from the server!");
			throw new Error(e)
		})
		await new Promise((resolve) => setTimeout(resolve, 1000))
	}
}

/**
 * @param {number} shipID
 */
function getShipFromID(shipID) {
	for (var i = 0; i < ships.length; i++) {
		if (ships[i].id == shipID) {
			return ships[i]
		}
	}
	throw new Error("Ship with id " + shipID + " not found!")
}
/**
 * @param {string} name
 * @returns {Team}
 */
function getTeamByName(name) {
	for (var i = 0; i < teams.length; i++) {
		if (teams[i].name == name) return teams[i]
	}
	throw new Error("Team with name " + name + " not found!")
}

function submitManeuvers() {
	if (me == null) return
	post("/maneuvers", [
		my_name,
		...me.ships.map((v) => v.maneuver ? v.id + " " + v.type.maneuvers.indexOf(v.maneuver) : "Error!")
	].join("\n"))
}

// ------------------------------ MAIN ------------------------------

map.addEventListener("mousedown", (e) => {
	if (activeMenu != null) {
		activeMenu.closeMenuWithLocation((e.clientX - viewportPos.x) / 2, (e.clientY - viewportPos.y) / 2)
		activeMenu = null
	}
	dragLoc = { x: e.clientX, y: e.clientY }
})
map.addEventListener("mousemove", (e) => {
	if (dragLoc != null) {
		var move = { x: e.clientX - dragLoc.x, y: e.clientY - dragLoc.y }
		dragLoc = { x: e.clientX, y: e.clientY }
		// move the viewport
		viewportPos.x += move.x
		viewportPos.y += move.y
		map.setAttribute("style", `background-position: ${viewportPos.x}px ${viewportPos.y}px;`)
		// update everything
		var elms = [...map.children]
		for (var i = 0; i < elms.length; i++) {
			elms[i].dispatchEvent(new Event("updatestyle"))
		}
	}
})
map.addEventListener("mouseup", (e) => {
	dragLoc = null
})



get("ships.txt").then((x) => {
	ship_types = parseShipList(x)
	setStatusBar()
}).then(eventCheckerLoop)

// In case you want to have a bot play the game :)

function autoready() {
	function c() {
		var btn = document.querySelector(".status > div:last-child > button")
		if (btn && (btn instanceof HTMLElement) && !btn.hasAttribute("disabled")) {
			btn.click()
		}
		if (gamePhase == "moving") setTimeout(c, 100)
		else console.log('did it')
	}
	c()
}
function autosetmaneuvers() {
	if (me == null) return
	if (gamePhase != "planning") return
	me.ships.forEach((v) => { v.maneuver = v.maneuver ?? v.type.maneuvers[Math.floor(Math.random() * v.type.maneuvers.length)]; v.updateStyle(); })
	var e = document.querySelector(".status > div:last-child > button")
	if (e) {
		e.removeAttribute("disabled")
		if (e instanceof HTMLElement) e.click()
	}
}
async function autoeverything() {
	while (true) {
		if (gamePhase == "planning") autosetmaneuvers()
		await new Promise((resolve) => setTimeout(resolve, 1000))
		autoready()
		await new Promise((resolve) => setTimeout(resolve, 1000))
	}
}
