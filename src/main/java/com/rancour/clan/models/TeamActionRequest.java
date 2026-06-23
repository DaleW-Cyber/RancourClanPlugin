package com.rancour.clan.models;

public final class TeamActionRequest
{
	private final String activeRsn;

	public TeamActionRequest(String activeRsn)
	{
		this.activeRsn = normalizeRsn(activeRsn);
	}

	public String getActiveRsn()
	{
		return activeRsn;
	}

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
