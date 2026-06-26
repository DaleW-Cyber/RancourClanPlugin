package com.rancour.clan.models;

import java.util.List;

public final class TeamEditRequest
{
	private final String activity;
	private final Integer capacity;
	private final Integer world;
	private final Boolean voiceRequired;
	private final String notes;
	private final List<String> tags;
	private final String status;

	public TeamEditRequest(String activity, Integer capacity, Integer world, Boolean voiceRequired, List<String> tags, String status)
	{
		this(activity, capacity, world, voiceRequired, null, tags, status);
	}

	public TeamEditRequest(String activity, Integer capacity, Integer world, Boolean voiceRequired, String notes, List<String> tags, String status)
	{
		this.activity = activity;
		this.capacity = capacity;
		this.world = world;
		this.voiceRequired = voiceRequired;
		this.notes = notes;
		this.tags = tags;
		this.status = status;
	}

	public String getActivity() { return activity; }
	public Integer getCapacity() { return capacity; }
	public Integer getWorld() { return world; }
	public Boolean getVoiceRequired() { return voiceRequired; }
	public String getNotes() { return notes; }
	public List<String> getTags() { return tags; }
	public String getStatus() { return status; }
}
