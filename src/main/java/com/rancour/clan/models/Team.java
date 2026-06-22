package com.rancour.clan.models;

import java.util.Collections;
import java.util.List;

public final class Team
{
	private final String id;
	private final String activity;
	private final String host;
	private final List<String> requiredRoles;
	private final int currentMembers;
	private final int capacity;
	private final int world;
	private final boolean voiceRequired;
	private final String status;
	private final boolean staffHosted;
	private final List<String> tags;
	private final boolean joined;

	public Team(String id, String activity, String host, List<String> requiredRoles, int currentMembers,
		int capacity, int world, boolean voiceRequired, String status, boolean staffHosted,
		List<String> tags, boolean joined)
	{
		this.id = id;
		this.activity = activity;
		this.host = host;
		this.requiredRoles = requiredRoles == null ? Collections.emptyList() : requiredRoles;
		this.currentMembers = currentMembers;
		this.capacity = capacity;
		this.world = world;
		this.voiceRequired = voiceRequired;
		this.status = status;
		this.staffHosted = staffHosted;
		this.tags = tags == null ? Collections.emptyList() : tags;
		this.joined = joined;
	}

	public String getId() { return id; }
	public String getActivity() { return activity; }
	public String getHost() { return host; }
	public List<String> getRequiredRoles() { return requiredRoles; }
	public int getCurrentMembers() { return currentMembers; }
	public int getCapacity() { return capacity; }
	public int getWorld() { return world; }
	public boolean isVoiceRequired() { return voiceRequired; }
	public String getStatus() { return status; }
	public boolean isStaffHosted() { return staffHosted; }
	public List<String> getTags() { return tags; }
	public boolean isJoined() { return joined; }
}
