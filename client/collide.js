const rotatedRectanglesCollide = (() => {
	const toRadians = (/** @type {number} */ degrees) => degrees * Math.PI / 180;
	// const fixFloat = (/** @type {number} */ number, precision=Math.log10(1/Number.EPSILON)) => number ? parseFloat(number.toFixed(precision)) : 0;

	class Vector {
		constructor({x=0,y=0}={}) {
			this.x = x;
			this.y = y;
		}
		get magnitude() {
			return Math.sqrt(this.x * this.x + this.y * this.y);
		}
		/**
		 * @param {Vector} factor
		 */
		Add(factor) {
			return new Vector({
				x: this.x + factor.x,
				y: this.y + factor.y,
			})
		}
		/**
		 * @param {Vector} factor
		 */
		Minus(factor) {
			return new Vector({
				x: this.x - factor.x,
				y: this.y - factor.y,
			})
		}
		/**
		 * @param {number | Vector} factor
		 */
		Multiply(factor) {
			const f = typeof factor === 'object'
				? factor
				: {x:factor, y:factor}
			return new Vector({
				x: this.x * f.x,
				y: this.y * f.y,
			})
		}

		/**
		 * @param {number} theta
		 */
		Rotate(theta) {
			return new Vector({
				x: this.x * Math.cos(theta) - this.y * Math.sin(theta),
				y: this.x * Math.sin(theta) + this.y * Math.cos(theta),
			});
		}
		/**
		 * @param {Line} line
		 */
		Project(line) {
			let dotvalue = line.direction.x * (this.x - line.origin.x)
				+ line.direction.y * (this.y - line.origin.y);
			return new Vector({
				x: line.origin.x + line.direction.x * dotvalue,
				y: line.origin.y + line.direction.y * dotvalue,
			})
		}
	}
	class Line {
		constructor({x=0,y=0, dx=0, dy=0}) {
			this.origin = new Vector({x,y});
			this.direction = new Vector({x:dx,y:dy});
		}
	  }
	  class Rect {
		constructor({
			x=0, y=0, w=10, h=10,
			// 0 is Horizontal to right (following OX) - Rotate clockwise
			theta=null, angle=0, // theta (rad) or angle (deg)
			rgb='0,0,0'
		}) {
			this.center = new Vector({x,y});
			this.size = new Vector({x:w,y:h});
			this.theta = theta || toRadians(angle);
			this.rgb = rgb;
		}
		getAxis() {
			const OX = new Vector({x:1, y:0});
			const OY = new Vector({x:0, y:1});
			const RX = OX.Rotate(this.theta);
			const RY = OY.Rotate(this.theta);
			return [
				new Line({...this.center, dx: RX.x, dy: RX.y}),
				new Line({...this.center, dx: RY.x, dy: RY.y}),
			];
		}
		getCorners() {
			const axis = this.getAxis();
			const RX = axis[0].direction.Multiply(this.size.x/2);
			const RY = axis[1].direction.Multiply(this.size.y/2);
			return [
				this.center.Add(RX).Add(RY),
				this.center.Add(RX).Add(RY.Multiply(-1)),
				this.center.Add(RX.Multiply(-1)).Add(RY.Multiply(-1)),
				this.center.Add(RX.Multiply(-1)).Add(RY),
			]
		}
		//removeme
		/**
		   * @param {Rect} other
		   */
		collidesWith(other){return isRectCollide(this,other)}
	}

	const isRectCollide = (/** @type {Rect} */ rectA, /** @type {Rect} */ rectB) => {
	 	return isProjectionCollide(rectA, rectB)
			&& isProjectionCollide(rectB, rectA);
	}
	const isProjectionCollide = (/** @type {Rect} */ rect, /** @type {Rect} */ onRect) => {
		const lines = onRect.getAxis();
		const corners = rect.getCorners();

		let isCollide = true;

		lines.forEach((line, dimension) => {
		/** @type {{min: null | { signedDistance: number, corner: Vector, projected: Vector }, max: null | { signedDistance: number, corner: Vector, projected: Vector }}} */
			let furthers = {min:null, max:null};
			// Size of onRect half size on line direction
			const rectHalfSize = (dimension === 0 ? onRect.size.x : onRect.size.y) / 2;
			corners.forEach(corner => {
				const projected = corner.Project(line);
				const CP = projected.Minus(onRect.center);
				// Sign: Same directon of OnRect axis : true.
				const sign = (CP.x * line.direction.x) + (CP.y * line.direction.y) > 0;
				const signedDistance = CP.magnitude * (sign ? 1 : -1);

				if (!furthers.min || furthers.min.signedDistance > signedDistance) {
					furthers.min = {signedDistance, corner, projected};
				}
				if (!furthers.max || furthers.max.signedDistance < signedDistance) {
					furthers.max = {signedDistance, corner, projected};
				}
			});
			// if (furthers.min != null && furthers.max != null)
			// @ts-ignore
			if (!(furthers.min.signedDistance < 0 && furthers.max.signedDistance > 0
				// @ts-ignore
				|| Math.abs(furthers.min.signedDistance) < rectHalfSize
				// @ts-ignore
				|| Math.abs(furthers.max.signedDistance) < rectHalfSize)) {
					isCollide = false;
				}
		});
		return isCollide;
	};

	function tests() {
		/**
		 * @param {number} x
		 * @param {number} y
		 * @param {number} w
		 * @param {number} h
		 * @param {number | undefined} [a]
		 */
		function rect(x, y, w, h, a) {
			if (a == undefined) {
				return rect(x - (w / 2), y - (w / 2), w, w, h);
			} else {
				return new Rect({ x: x + (w / 2), y: y + (h / 2), w, h, angle: a })
			}
		}
		/*
			┌─────┐
			│  2  │
			│    ┌┼────┐
			└────┼┘ 1  │
			     │     │
			     └─────┘
		 */
		var r1 = rect(0, 0, 10, 10, 0);
		var r2 = rect(-5, -5, 10.1, 0);
		if (!r1.collidesWith(r2)) throw new Error("Test case did not pass!")
		/*
			 ┌─────┐
			 │  1  │
			 │     │
			 └─────┘
					╱╲ 3
					╲╱
		 */
		var r3 = rect(10, 10, 1, 1, 45); // does not touch due to rotation
		if (!!r1.collidesWith(r3)) throw new Error("Test case did not pass!")
		/*
			 ┌─────┐
			 │  1  │
			 │     │
			 └─────┼─────┐
				   │  4  │
				   │     │
				   └─────┘
		 */
		var r4 = rect(10, 10, 1, 1, 0); // touches exactly at corner
		// NOTE: This does NOT register as touching, because the area where the two
		// rectangles collide does not have any area.
		if (!!r1.collidesWith(r4)) throw new Error("Test case did not pass!")
		var r4b = rect(9.9, 9.9, 1, 1, 0); // Teeny overlap at corner
		if (!r1.collidesWith(r4b)) throw new Error("Test case did not pass!")
		/*
			  ╱╲  ╱╲
			 ╱ 6╲╱5 ╲
			 ╲  ╱╲  ╱
			  ╲╱  ╲╱
		 */
		var r5 = rect(2, 2, 2 * 1.415, -45); // length slightly longer than √2
		var r6 = rect(-2, 2, 2 * 1.415, 45);
		if (!r5.collidesWith(r6)) throw new Error("Test case did not pass!") // rotated rects touch at (5, 0)
		/*
			 ▁▂▃▄▅ 7
			 ███▀▔
			 ┞─────┐
			 │  8  │
			 └─────┘
		 */
		var r7 = rect(5, 5, 4, 10); // Tilted 10° up.
		var r8 = rect(2, 0, 1.75, 2.75, 0);  // Right below r7, overlaps due to rotation
		if (!r7.collidesWith(r8)) throw new Error("Test case did not pass!")
		if (!r8.collidesWith(r7)) throw new Error("Test case did not pass!")
		/*
			 ▁▂▃▄▅ 7
			 ███▀▔█▇▅▃▁
				  ▔▀███ 9
		 */
		var r9 = rect(7, 0, 5, 3, -10); // Titled 10° down, overlaps r7 due to rotation
		if (!r9.collidesWith(r7)) throw new Error("Test case did not pass!")
		if (!r7.collidesWith(r9)) throw new Error("Test case did not pass!")
		/*
			 ▁▂▃▄▅ 7
			 ███▀▔
			 ▁▂▃▄▅ 10
			 ███▀▔
		 */
		var r10 = rect(6, 0, 5, 3, 10); // Titled 10° up, does NOT overlap r7 due to rotation
		if (!!r10.collidesWith(r7)) throw new Error("Test case did not pass!")
		if (!!r7.collidesWith(r10)) throw new Error("Test case did not pass!")
		/*
			 ▁▂▃▄▅ 7
			 ███▀▔
				┌─────┐
				│ 11  │
				└─────┘
		 */
		var r11 = rect(6, 0, 5, 3, 0); // does NOT overlap r7 due to r7's rotation
		if (!!r11.collidesWith(r7)) throw new Error("Test case did not pass!")
		if (!!r7.collidesWith(r11)) throw new Error("Test case did not pass!")

		console.log("All test cases have passed.")
	}
	// Return
	/**
	 * @param {number} cx1
	 * @param {number} cy1
	 * @param {number} w1
	 * @param {number} h1
	 * @param {number} rot1
	 * @param {number} cx2
	 * @param {number} cy2
	 * @param {number} w2
	 * @param {number} h2
	 * @param {number} rot2
	 */
	function collide(cx1, cy1, w1, h1, rot1, cx2, cy2, w2, h2, rot2) {
		var r1 = new Rect({ x: cx1, y: cy1, w: w1, h: h1, angle: rot1 })
		var r2 = new Rect({ x: cx2, y: cy2, w: w2, h: h2, angle: rot2 })
		return isRectCollide(r1, r2)
	}
	return collide;
})();