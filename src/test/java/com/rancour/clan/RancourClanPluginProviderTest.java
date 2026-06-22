package com.rancour.clan;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import javax.inject.Singleton;
import org.junit.Test;

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
}
