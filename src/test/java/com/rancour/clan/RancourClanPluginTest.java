package com.rancour.clan;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RancourClanPluginTest
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("Loading Rancour Clan plugin for local RuneLite development...");
		ExternalPluginManager.loadBuiltin(RancourClanPlugin.class);
		System.out.println("Rancour Clan plugin registered with RuneLite ExternalPluginManager.");
		RuneLite.main(args);
	}
}
