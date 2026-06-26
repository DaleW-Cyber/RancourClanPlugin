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
	private final String notes;
	private final String status;
	private final boolean staffHosted;
	private final List<String> tags;
	private final boolean joined;
	private final List<String> joinedMembers;
	private final String createdAt;
	private final String expiresAt;
	private final String fullAt;
	private final String readyNotifiedAt;
	private final String closedAt;
	private final Boolean notifyCurrentUser;

	public Team(String id, String activity, String host, List<String> requiredRoles, int currentMembers,
		int capacity, int world, boolean voiceRequired, String status, boolean staffHosted,
		List<String> tags, boolean joined)
	{
		this(id, activity, host, requiredRoles, currentMembers, capacity, world, voiceRequired, status,
			staffHosted, tags, joined, Collections.emptyList(), null, null, null, null, null, "", null);
	}

	public Team(String id, String activity, String host, List<String> requiredRoles, int currentMembers,
		int capacity, int world, boolean voiceRequired, String status, boolean staffHosted,
		List<String> tags, boolean joined, List<String> joinedMembers, String createdAt, String expiresAt,
		String fullAt, String closedAt)
	{
		this(id, activity, host, requiredRoles, currentMembers, capacity, world, voiceRequired, status,
			staffHosted, tags, joined, joinedMembers, createdAt, expiresAt, fullAt, null, closedAt, "", null);
	}

	public Team(String id, String activity, String host, List<String> requiredRoles, int currentMembers,
		int capacity, int world, boolean voiceRequired, String status, boolean staffHosted,
		List<String> tags, boolean joined, List<String> joinedMembers, String createdAt, String expiresAt,
		String fullAt, String closedAt, String notes)
	{
		this(id, activity, host, requiredRoles, currentMembers, capacity, world, voiceRequired, status,
			staffHosted, tags, joined, joinedMembers, createdAt, expiresAt, fullAt, null, closedAt, notes, null);
	}

	public Team(String id, String activity, String host, List<String> requiredRoles, int currentMembers,
		int capacity, int world, boolean voiceRequired, String status, boolean staffHosted,
		List<String> tags, boolean joined, List<String> joinedMembers, String createdAt, String expiresAt,
		String fullAt, String readyNotifiedAt, String closedAt, String notes)
	{
		this(id, activity, host, requiredRoles, currentMembers, capacity, world, voiceRequired, status,
			staffHosted, tags, joined, joinedMembers, createdAt, expiresAt, fullAt, readyNotifiedAt, closedAt, notes, null);
	}

	public Team(String id, String activity, String host, List<String> requiredRoles, int currentMembers,
		int capacity, int world, boolean voiceRequired, String status, boolean staffHosted,
		List<String> tags, boolean joined, List<String> joinedMembers, String createdAt, String expiresAt,
		String fullAt, String readyNotifiedAt, String closedAt, String notes, Boolean notifyCurrentUser)
	{
		this.id = id;
		this.activity = activity;
		this.host = host;
		this.requiredRoles = requiredRoles == null ? Collections.emptyList() : requiredRoles;
		this.currentMembers = currentMembers;
		this.capacity = capacity;
		this.world = world;
		this.voiceRequired = voiceRequired;
		this.notes = notes;
		this.status = status;
		this.staffHosted = staffHosted;
		this.tags = tags == null ? Collections.emptyList() : tags;
		this.joined = joined;
		this.joinedMembers = joinedMembers == null ? Collections.emptyList() : joinedMembers;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		this.fullAt = fullAt;
		this.readyNotifiedAt = readyNotifiedAt;
		this.closedAt = closedAt;
		this.notifyCurrentUser = notifyCurrentUser;
	}

	public String getId() { return id; }
	public String getActivity() { return activity; }
	public String getHost() { return host; }
	public List<String> getRequiredRoles() { return requiredRoles; }
	public int getCurrentMembers() { return currentMembers; }
	public int getCapacity() { return capacity; }
	public int getWorld() { return world; }
	public boolean isVoiceRequired() { return voiceRequired; }
	public String getNotes() { return notes; }
	public String getStatus() { return status; }
	public boolean isStaffHosted() { return staffHosted; }
	public List<String> getTags() { return tags; }
	public boolean isJoined() { return joined; }
	public List<String> getJoinedMembers() { return joinedMembers; }
	public String getCreatedAt() { return createdAt; }
	public String getExpiresAt() { return expiresAt; }
	public String getFullAt() { return fullAt; }
	public String getReadyNotifiedAt() { return readyNotifiedAt; }
	public String getClosedAt() { return closedAt; }
	public boolean shouldNotifyCurrentUser() { return notifyCurrentUser == null || notifyCurrentUser; }
}
