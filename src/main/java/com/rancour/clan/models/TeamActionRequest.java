package com.rancour.clan.models;

public final class TeamActionRequest
{
	private final String activeRsn;

	public TeamActionRequest(String activeRsn)
	{
		this.activeRsn = activeRsn;
	}

	public String getActiveRsn()
	{
		return activeRsn;
	}
}
