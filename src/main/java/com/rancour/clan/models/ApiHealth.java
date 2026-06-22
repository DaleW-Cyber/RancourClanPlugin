package com.rancour.clan.models;

public final class ApiHealth
{
	private final String status;

	public ApiHealth(String status)
	{
		this.status = status;
	}

	public String getStatus()
	{
		return status;
	}
}
