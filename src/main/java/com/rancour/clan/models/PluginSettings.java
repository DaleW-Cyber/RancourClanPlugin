package com.rancour.clan.models;

import java.util.Collections;
import java.util.List;

public final class PluginSettings
{
	private final boolean dropsPanelEnabled;
	private final String dropsAccessMode;
	private final Boolean dropsVisible;
	private final Boolean dropsCanSubmit;
	private final String dropsRestrictionMessage;
	private final List<String> approvedDrops;
	private final List<ApprovedDropSource> approvedDropSources;

	public PluginSettings(boolean dropsPanelEnabled, List<String> approvedDrops)
	{
		this(dropsPanelEnabled, approvedDrops, Collections.emptyList());
	}

	public PluginSettings(boolean dropsPanelEnabled, List<String> approvedDrops, List<ApprovedDropSource> approvedDropSources)
	{
		this(dropsPanelEnabled, "members", null, null, null, approvedDrops, approvedDropSources);
	}

	public PluginSettings(boolean dropsPanelEnabled, String dropsAccessMode, Boolean dropsVisible,
		Boolean dropsCanSubmit, String dropsRestrictionMessage, List<String> approvedDrops,
		List<ApprovedDropSource> approvedDropSources)
	{
		this.dropsPanelEnabled = dropsPanelEnabled;
		this.dropsAccessMode = dropsAccessMode;
		this.dropsVisible = dropsVisible;
		this.dropsCanSubmit = dropsCanSubmit;
		this.dropsRestrictionMessage = dropsRestrictionMessage;
		this.approvedDrops = approvedDrops == null ? Collections.emptyList() : approvedDrops;
		this.approvedDropSources = approvedDropSources == null ? Collections.emptyList() : approvedDropSources;
	}

	public boolean isDropsPanelEnabled()
	{
		return dropsPanelEnabled;
	}

	public String getDropsAccessMode()
	{
		return dropsAccessMode == null || dropsAccessMode.trim().isEmpty() ? "members" : dropsAccessMode;
	}

	public boolean isDropsVisible()
	{
		return dropsVisible == null ? dropsPanelEnabled : dropsVisible;
	}

	public boolean canSubmitDrops()
	{
		return dropsCanSubmit == null ? dropsPanelEnabled : dropsCanSubmit;
	}

	public String getDropsRestrictionMessage()
	{
		return dropsRestrictionMessage;
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
