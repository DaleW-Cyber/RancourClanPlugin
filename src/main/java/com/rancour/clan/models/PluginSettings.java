package com.rancour.clan.models;

import java.util.Collections;
import java.util.List;

public final class PluginSettings
{
	private final boolean dropsPanelEnabled;
	private final List<String> approvedDrops;
	private final List<ApprovedDropSource> approvedDropSources;

	public PluginSettings(boolean dropsPanelEnabled, List<String> approvedDrops)
	{
		this(dropsPanelEnabled, approvedDrops, Collections.emptyList());
	}

	public PluginSettings(boolean dropsPanelEnabled, List<String> approvedDrops, List<ApprovedDropSource> approvedDropSources)
	{
		this.dropsPanelEnabled = dropsPanelEnabled;
		this.approvedDrops = approvedDrops == null ? Collections.emptyList() : approvedDrops;
		this.approvedDropSources = approvedDropSources == null ? Collections.emptyList() : approvedDropSources;
	}

	public boolean isDropsPanelEnabled()
	{
		return dropsPanelEnabled;
	}

	public List<String> getApprovedDrops()
	{
		return approvedDrops;
	}

	public List<ApprovedDropSource> getApprovedDropSources()
	{
		return approvedDropSources;
	}

	public static final class ApprovedDropSource
	{
		private final String boss;
		private final List<String> drops;

		public ApprovedDropSource(String boss, List<String> drops)
		{
			this.boss = boss;
			this.drops = drops == null ? Collections.emptyList() : drops;
		}

		public String getBoss()
		{
			return boss;
		}

		public List<String> getDrops()
		{
			return drops;
		}
	}
}
