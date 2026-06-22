package com.rancour.clan.config;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(RancourClanConfig.GROUP)
public interface RancourClanConfig extends Config
{
	String GROUP = "rancourclan";

	@ConfigItem(
		keyName = "apiBaseUrl",
		name = "API base URL",
		description = "Base URL for the future Rancour REST API"
	)
	default String apiBaseUrl()
	{
		return "https://api.rancourpvm.com";
	}

	@ConfigItem(
		keyName = "mockMode",
		name = "Mock mode",
		description = "Use clearly labelled local development data instead of the Rancour API"
	)
	default boolean mockMode()
	{
		return false;
	}

	@ConfigItem(
		keyName = "announcementChatNotifications",
		name = "Show announcement notifications in chat",
		description = "Show a short game-chat message when the API returns a new announcement"
	)
	default boolean announcementChatNotifications()
	{
		return true;
	}

	@ConfigItem(
		keyName = "announcementMinimumPriority",
		name = "Minimum announcement priority",
		description = "Lowest announcement priority shown in game chat"
	)
	default AnnouncementPriority announcementMinimumPriority()
	{
		return AnnouncementPriority.NORMAL;
	}

	@ConfigItem(
		keyName = "automaticRefresh",
		name = "Enable automatic refresh",
		description = "Periodically refresh verification, announcements, events, teams, and staff queues"
	)
	default boolean automaticRefresh()
	{
		return true;
	}

	@ConfigItem(
		keyName = "refreshIntervalSeconds",
		name = "Refresh interval seconds",
		description = "Automatic refresh interval. Values below 30 seconds are treated as 30."
	)
	default int refreshIntervalSeconds()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "minimumDropValue",
		name = "Minimum drop value",
		description = "Minimum total GE value for NPC loot confirmation prompts"
	)
	default int minimumDropValue()
	{
		return 1_000_000;
	}
}
