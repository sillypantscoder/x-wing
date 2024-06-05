package com.sillypantscoder.xwing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
}
