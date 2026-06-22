package com.rancour.clan;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RancourClanPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(RancourClanPlugin.class);
        RuneLite.main(args);
    }
}
