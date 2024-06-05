package com.sillypantscoder.xwing;

public class Point {
	public int x;
	public int y;
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Point(Point other) {
		this.x = other.x;
		this.y = other.y;
	}
	public Point moveDirection(int deg, int dist) {
		Point r = new Point(this);
		r.x += dist * Math.cos(Math.toRadians(deg));
		r.y += dist * Math.sin(Math.toRadians(deg));
		return r;
	}
	public String toString() {
		return "Point { x: " + x + ", y: " + y + " }";
	}
}
