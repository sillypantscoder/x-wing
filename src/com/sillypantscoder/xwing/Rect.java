package com.sillypantscoder.xwing;

public class Rect {
	public double x;
	public double y;
	public double w;
	public double h;
	public double rot;
	public Rect(double x, double y, double w, double h, double rot) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.rot = rot;
	}
	public Rect(double cx, double cy, double size, double rot) {
		this.x = cx - (size / 2);
		this.y = cy - (size / 2);
		this.w = size;
		this.h = size;
		this.rot = rot;
	}
	public boolean collidesWith(Rect other) {
		return Utils.rotatedRectanglesCollide(x, y, w, h, rot, other.x, other.y, other.w, other.h, other.rot);
	}
	public String toString() {
		return "Rect { x: " + x + ", y: " + y + ", w: " + w + ", h: " + h + ", r: " + rot + " }";
	}
	public static void main(String[] args) {
		/*
			┌─────┐
			│  2  │
			│    ┌┼────┐
			└────┼┘ 1  │
			     │     │
			     └─────┘
		 */
		Rect r1 = new Rect(0, 0, 10, 10, 0);
		Rect r2 = new Rect(-5, -5, 10.1, 0);
		assert r1.collidesWith(r2);
		/*
			┌─────┐
			│  1  │
			│     │
			└─────┘
			       ╱╲ 3
			       ╲╱
		 */
		Rect r3 = new Rect(10, 10, 1, 1, 45); // does not touch due to rotation
		assert !r1.collidesWith(r3);
		/*
			┌─────┐
			│  1  │
			│     │
			└─────┼─────┐
			      │  4  │
			      │     │
			      └─────┘
		 */
		Rect r4 = new Rect(10, 10, 1, 1, 0); // touches exactly at corner
		// NOTE: This does NOT register as touching, because the area where the two
		// rectangles collide does not have any area.
		assert !r1.collidesWith(r4);
		Rect r4b = new Rect(9.9, 9.9, 1, 1, 0); // Teeny overlap at corner
		assert r1.collidesWith(r4b);
		/*
			 ╱╲  ╱╲
			╱ 6╲╱5 ╲
			╲  ╱╲  ╱
			 ╲╱  ╲╱
		 */
		Rect r5 = new Rect(2, 2, 2 * 1.415, -45); // length slightly longer than √2
		Rect r6 = new Rect(-2, 2, 2 * 1.415, 45);
		assert r5.collidesWith(r6); // rotated rects touch at (5, 0)
		/*
			▁▂▃▄▅ 7
			███▀▔
			┞─────┐
			│  8  │
			└─────┘
		 */
		Rect r7 = new Rect(5, 5, 4, 10); // Tilted 10° up.
		Rect r8 = new Rect(2, 0, 1.75, 2.75, 0);  // Right below r7, overlaps due to rotation
		assert r7.collidesWith(r8);
		assert r8.collidesWith(r7);
		/*
			▁▂▃▄▅ 7
			███▀▔█▇▅▃▁
			     ▔▀███ 9
		 */
		Rect r9 = new Rect(7, 0, 5, 3, -10); // Titled 10° down, overlaps r7 due to rotation
		assert r9.collidesWith(r7);
		assert r7.collidesWith(r9);
		/*
			▁▂▃▄▅ 7
			███▀▔
			▁▂▃▄▅ 10
			███▀▔
		 */
		Rect r10 = new Rect(6, 0, 5, 3, 10); // Titled 10° up, does NOT overlap r7 due to rotation
		assert !r10.collidesWith(r7);
		assert !r7.collidesWith(r10);
		/*
			▁▂▃▄▅ 7
			███▀▔
			   ┌─────┐
			   │ 11  │
			   └─────┘
		 */
		Rect r11 = new Rect(6, 0, 5, 3, 0); // does NOT overlap r7 due to r7's rotation
		assert !r11.collidesWith(r7);
		assert !r7.collidesWith(r11);
	}
}
