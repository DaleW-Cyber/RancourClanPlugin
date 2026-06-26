package com.rancour.clan.models;

public final class DropsAccessSettingRequest
{
	private final String mode;

	public DropsAccessSettingRequest(String mode)
	{
		this.mode = mode;
	}

	public String getMode()
	{
		return mode;
	}
}
