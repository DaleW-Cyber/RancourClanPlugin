package com.rancour.clan.models;

public final class ClanEvent
{
	private final String name;
	private final String schedule;
	private final String details;

	public ClanEvent(String name, String schedule, String details)
	{
		this.name = name;
		this.schedule = schedule;
		this.details = details;
	}

	public String getName() { return name; }
	public String getSchedule() { return schedule; }
	public String getDetails() { return details; }
}
