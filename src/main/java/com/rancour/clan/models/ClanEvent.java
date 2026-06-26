package com.rancour.clan.models;

import java.util.List;

public final class ClanEvent
{
	private final String id;
	private final String name;
	private final String startTime;
	private final String endTime;
	private final String description;
	private final String notes;
	private final String host;
	private final String status;
	private final int signupCount;
	private final boolean joined;
	private final String visibility;
	private final List<String> requiredRoleIds;
	private final String sourceChannelId;

	public ClanEvent(String id, String name, String startTime, String description, String host,
		String status, int signupCount, boolean joined, String visibility, List<String> requiredRoleIds,
		String sourceChannelId)
	{
		this(id, name, startTime, null, description, description, host, status, signupCount, joined, visibility,
			requiredRoleIds, sourceChannelId);
	}

	public ClanEvent(String id, String name, String startTime, String description, String notes, String host,
		String status, int signupCount, boolean joined, String visibility, List<String> requiredRoleIds,
		String sourceChannelId)
	{
		this(id, name, startTime, null, description, notes, host, status, signupCount, joined, visibility,
			requiredRoleIds, sourceChannelId);
	}

	public ClanEvent(String id, String name, String startTime, String endTime, String description, String notes, String host,
		String status, int signupCount, boolean joined, String visibility, List<String> requiredRoleIds,
		String sourceChannelId)
	{
		this.id = id;
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.description = description;
		this.notes = notes;
		this.host = host;
		this.status = status;
		this.signupCount = signupCount;
		this.joined = joined;
		this.visibility = visibility;
		this.requiredRoleIds = requiredRoleIds;
		this.sourceChannelId = sourceChannelId;
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public String getStartTime() { return startTime; }
	public String getEndTime() { return endTime; }
	public String getDescription() { return description; }
	public String getNotes() { return notes == null || notes.trim().isEmpty() ? description : notes; }
	public String getHost() { return host; }
	public String getStatus() { return status; }
	public int getSignupCount() { return signupCount; }
	public boolean isJoined() { return joined; }
	public String getVisibility() { return visibility; }
	public List<String> getRequiredRoleIds() { return requiredRoleIds; }
	public String getSourceChannelId() { return sourceChannelId; }
}
