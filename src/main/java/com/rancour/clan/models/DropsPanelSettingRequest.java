package com.rancour.clan.models;

public final class DropsPanelSettingRequest
{
	private final boolean enabled;

	public DropsPanelSettingRequest(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isEnabled()
	{
		return enabled;
	}
}
