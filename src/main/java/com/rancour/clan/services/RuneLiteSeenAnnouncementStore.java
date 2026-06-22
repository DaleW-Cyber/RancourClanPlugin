package com.rancour.clan.services;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.client.config.ConfigManager;
import com.rancour.clan.config.RancourClanConfig;

public final class RuneLiteSeenAnnouncementStore implements SeenAnnouncementStore
{
	private static final String KEY = "seenAnnouncementIds";
	private final ConfigManager configManager;

	public RuneLiteSeenAnnouncementStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	@Override
	public Set<String> load()
	{
		String value = configManager.getConfiguration(RancourClanConfig.GROUP, KEY);
		if (value == null || value.trim().isEmpty())
		{
			return new LinkedHashSet<>();
		}
		return Arrays.stream(value.split(","))
			.map(String::trim)
			.filter(item -> !item.isEmpty())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public void save(Set<String> announcementIds)
	{
		configManager.setConfiguration(RancourClanConfig.GROUP, KEY, String.join(",", announcementIds));
	}
}
