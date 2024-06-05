package com.sillypantscoder.xwing;

public class Rect {
	public double x;
	public double y;
	public double w;
	public double h;
	public double r;
	public Rect(double x, double y, double w, double h, double r) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.r = r;
	}
	public Rect(double cx, double cy, double size, double r) {
		this.x = cx - (size / 2);
		this.y = cy - (size / 2);
		this.w = size;
		this.h = size;
		this.r = r;
	}
	public boolean collidesWith(Rect other) {
		return Utils.rotatedRectanglesCollide(x, y, w, h, r, other.x, other.y, other.w, other.h, other.r);
	}
	public String toString() {
		return "Rect { x: " + x + ", y: " + y + ", w: " + w + ", h: " + h + ", r: " + r + " }";
	}
}
