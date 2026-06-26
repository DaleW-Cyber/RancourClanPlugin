package com.rancour.clan.services;

import net.runelite.client.config.ConfigManager;

public final class RuneLiteSeenTeamReadyStore implements SeenTeamReadyStore
{
	private static final String GROUP = "rancourclan";
	private static final String DEFAULT_KEY_PREFIX = "seenTeamReady.";
	private final String keyPrefix;
	private final ConfigManager configManager;

	public RuneLiteSeenTeamReadyStore(ConfigManager configManager)
	{
		this(configManager, DEFAULT_KEY_PREFIX);
	}

	public RuneLiteSeenTeamReadyStore(ConfigManager configManager, String keyPrefix)
	{
		this.configManager = configManager;
		this.keyPrefix = keyPrefix;
	}

	@Override
	public boolean isSeen(String teamId)
	{
		return Boolean.TRUE.equals(configManager.getConfiguration(GROUP, keyPrefix + teamId, Boolean.class));
	}

	@Override
	public void markSeen(String teamId)
	{
		configManager.setConfiguration(GROUP, keyPrefix + teamId, true);
	}
}
