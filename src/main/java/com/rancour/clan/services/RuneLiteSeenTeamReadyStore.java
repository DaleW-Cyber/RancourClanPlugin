package com.rancour.clan.services;

import net.runelite.client.config.ConfigManager;

public final class RuneLiteSeenTeamReadyStore implements SeenTeamReadyStore
{
	private static final String GROUP = "rancourclan";
	private static final String KEY_PREFIX = "seenTeamReady.";
	private final ConfigManager configManager;

	public RuneLiteSeenTeamReadyStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	@Override
	public boolean isSeen(String teamId)
	{
		return Boolean.TRUE.equals(configManager.getConfiguration(GROUP, KEY_PREFIX + teamId, Boolean.class));
	}

	@Override
	public void markSeen(String teamId)
	{
		configManager.setConfiguration(GROUP, KEY_PREFIX + teamId, true);
	}
}
