package com.rancour.clan.models;

import java.util.Collections;
import java.util.List;

public final class PluginSettings
{
	private final boolean dropsPanelEnabled;
	private final List<String> approvedDrops;

	public PluginSettings(boolean dropsPanelEnabled, List<String> approvedDrops)
	{
		this.dropsPanelEnabled = dropsPanelEnabled;
		this.approvedDrops = approvedDrops == null ? Collections.emptyList() : approvedDrops;
	}

	public boolean isDropsPanelEnabled()
	{
		return dropsPanelEnabled;
	}

	public List<String> getApprovedDrops()
	{
		return approvedDrops;
	}
}
