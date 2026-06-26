package com.rancour.clan.models;

public final class PluginDropsApprovalSettingRequest
{
	private final boolean requireApproval;

	public PluginDropsApprovalSettingRequest(boolean requireApproval)
	{
		this.requireApproval = requireApproval;
	}

	public boolean isRequireApproval()
	{
		return requireApproval;
	}
}
