package com.rancour.clan.models;

import java.util.Collections;
import java.util.List;

public final class TeamCreateRequest
{
	private final String activity;
	private final int capacity;
	private final int world;
	private final boolean voiceRequired;
	private final String notes;
	private final String activeRsn;
	private final List<String> tags;

	public TeamCreateRequest(String activity, int capacity, int world, boolean voiceRequired, String notes, String activeRsn)
	{
		this(activity, capacity, world, voiceRequired, notes, activeRsn, Collections.emptyList());
	}

	public TeamCreateRequest(String activity, int capacity, int world, boolean voiceRequired, String notes, String activeRsn,
		List<String> tags)
	{
		this.activity = activity;
		this.capacity = capacity;
		this.world = world;
		this.voiceRequired = voiceRequired;
		this.notes = notes;
		this.activeRsn = normalizeRsn(activeRsn);
		this.tags = tags == null ? Collections.emptyList() : tags;
	}

	public String getActivity() { return activity; }
	public int getCapacity() { return capacity; }
	public int getWorld() { return world; }
	public boolean isVoiceRequired() { return voiceRequired; }
	public String getNotes() { return notes; }
	public String getActiveRsn() { return activeRsn; }
	public List<String> getTags() { return tags; }

	private static String normalizeRsn(String value)
	{
		if (value == null)
		{
			return null;
		}
		String normalized = value.replace('\u00A0', ' ').trim().replaceAll("\\s+", " ");
		return normalized.isEmpty() || normalized.length() > 12 ? null : normalized;
	}
}
