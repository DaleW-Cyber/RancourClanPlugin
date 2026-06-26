package com.rancour.clan.config;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(RancourClanConfig.GROUP)
public interface RancourClanConfig extends Config
{
	String GROUP = "rancourclan";

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
		keyName = "notifyTeamCreated",
		name = "Notify when a new team is formed",
		description = "Show a short game-chat message when a new team is created"
	)
	default boolean notifyTeamCreated()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifyTeamFull",
		name = "Notify when my team is full",
		description = "Show a short game-chat message when a joined team reaches capacity"
	)
	default boolean notifyTeamFull()
	{
		return true;
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
}
