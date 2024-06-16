package com.sillypantscoder.xwing;

import java.util.ArrayList;

public class Team {
	public static Team[] teams = getTeams();
	public String name;
	public String[] colors;
	public Team(String name, String[] colors) {
		this.name = name;
		this.colors = colors;
	}
	public static Team[] getTeams() {
		String[] info = Utils.readFile("ships.txt").split("\n\n");
		ArrayList<Team> parsed = new ArrayList<Team>();
		for (var i = 0; i < info.length; i++) {
			String[] lines = info[i].split("\n");
			if (lines[0].startsWith("TEAM")) {
				// New team
				String name = lines[0].substring(5);
				String[] colors = lines[1].substring(8).split(" ");
				parsed.add(new Team(name, colors));
			}
		}
		return Utils.arrayListToArray(parsed, Team.class);
	}
}
