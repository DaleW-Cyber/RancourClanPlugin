package com.rancour.clan.models;

public final class ClanEvent
{
	private final String id;
	private final String name;
	private final String startTime;
	private final String description;
	private final String host;
	private final String status;
	private final int signupCount;
	private final boolean joined;

	public ClanEvent(String id, String name, String startTime, String description, String host,
		String status, int signupCount, boolean joined)
	{
		this.id = id;
		this.name = name;
		this.startTime = startTime;
		this.description = description;
		this.host = host;
		this.status = status;
		this.signupCount = signupCount;
		this.joined = joined;
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public String getStartTime() { return startTime; }
	public String getDescription() { return description; }
	public String getHost() { return host; }
	public String getStatus() { return status; }
	public int getSignupCount() { return signupCount; }
	public boolean isJoined() { return joined; }
}
