package com.sillypantscoder.xwing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class Utils {
	public static<T> void log(T item) {
		System.out.println(getLog(item));
	}
	public static<T> String getLog(T item) {
		if (item instanceof String) return "\"" + item + "\"";
		else if (item instanceof Number) return "{" + item + "}";
		else if (item instanceof Object[] itemList) return logArray(itemList);
		else {
			String s = item.toString();
			return getLog(s);
		}
	}
	public static<T> String logArray(T[] items) {
		String result = "[";
		String[] strItems = new String[items.length];
		for (var i = 0; i < items.length; i++) strItems[i] = items[i].toString();
		for (var i = 0; i < strItems.length; i++) {
			if (i != 0) {
				result += ", ";
			}
			result += getLog(strItems[i]);
		}
		result += "]";
		return result;
	}
	public static String readFile(String name) {
		try {
			File f = new File(name);
			byte[] bytes = new byte[(int)(f.length())];
			FileInputStream fis = new FileInputStream(f);
			fis.read(bytes);
			fis.close();
			return new String(bytes);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	public static boolean rotatedRectanglesCollide(double x1, double y1, double w1, double h1, double r1, double x2, double y2, double w2, double h2, double r2) {
		Area area1 = new Area(new Rectangle2D.Double(x1, y1, w1, h1));
		Area area2 = new Area(new Rectangle2D.Double(x2, y2, w2, h2));
		area1.transform(AffineTransform.getRotateInstance(Math.toRadians(r1), x1 + w1 / 2, y1 + h1 / 2));
		area2.transform(AffineTransform.getRotateInstance(Math.toRadians(r2), x2 + w2 / 2, y2 + h2 / 2));
		Area collision = new Area(area1);
		collision.intersect(area2);
		return !collision.isEmpty();
	}
	@SuppressWarnings("unchecked")
	public static<T> T[] arrayListToArray(ArrayList<T> list, Class<T> type) {
		T[] array = (T[])(Array.newInstance(type, list.size()));
		return list.toArray(array);
	}
}
