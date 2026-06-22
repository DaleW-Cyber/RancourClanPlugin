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
}
