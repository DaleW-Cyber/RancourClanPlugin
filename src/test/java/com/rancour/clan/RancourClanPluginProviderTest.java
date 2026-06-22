package com.rancour.clan;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import javax.inject.Singleton;
import org.junit.Test;
import net.runelite.client.config.ConfigItem;

public class RancourClanPluginProviderTest
{
	@Test
	public void verificationProviderIsSingletonSoStaffActionsShareProfileState() throws Exception
	{
		Method method = RancourClanPlugin.class.getDeclaredMethod(
			"provideVerificationService",
			com.rancour.clan.api.ClanApiClient.class,
			com.rancour.clan.services.SessionStore.class
		);

		assertNotNull(method.getAnnotation(Singleton.class));
	}

	@Test
	public void publicConfigDoesNotExposeMockModeOrApiBaseUrl()
	{
		for (Method method : com.rancour.clan.config.RancourClanConfig.class.getDeclaredMethods())
		{
			ConfigItem item = method.getAnnotation(ConfigItem.class);
			if (item == null)
			{
				continue;
			}
			org.junit.Assert.assertNotEquals("mockMode", item.keyName());
			org.junit.Assert.assertNotEquals("apiBaseUrl", item.keyName());
		}
	}
}
