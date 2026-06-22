package com.rancour.clan;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("rancourclan")
public interface RancourClanConfig extends Config
{
    @ConfigItem(
        keyName = "apiBaseUrl",
        name = "API Base URL",
        description = "Base URL for the Rancour clan plugin API."
    )
    default String apiBaseUrl()
    {
        return "https://api.rancourpvm.com";
    }
}
