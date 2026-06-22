package com.rancour.clan.services;

import net.runelite.client.config.ConfigManager;
import com.rancour.clan.config.RancourClanConfig;

public final class RuneLiteSessionStore implements SessionStore
{
	private static final String SESSION_TOKEN = "sessionToken";
	private static final String PENDING_VERIFICATION_ID = "pendingVerificationId";
	private final ConfigManager configManager;

	public RuneLiteSessionStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	@Override public String getSessionToken() { return value(SESSION_TOKEN); }
	@Override public String getPendingVerificationId() { return value(PENDING_VERIFICATION_ID); }

	@Override
	public void setSessionToken(String token)
	{
		setOrUnset(SESSION_TOKEN, token);
	}

	@Override
	public void setPendingVerificationId(String verificationId)
	{
		setOrUnset(PENDING_VERIFICATION_ID, verificationId);
	}

	@Override
	public void clear()
	{
		configManager.unsetConfiguration(RancourClanConfig.GROUP, SESSION_TOKEN);
		configManager.unsetConfiguration(RancourClanConfig.GROUP, PENDING_VERIFICATION_ID);
	}

	private String value(String key)
	{
		String value = configManager.getConfiguration(RancourClanConfig.GROUP, key);
		return value == null ? "" : value;
	}

	private void setOrUnset(String key, String value)
	{
		if (value == null || value.trim().isEmpty())
		{
			configManager.unsetConfiguration(RancourClanConfig.GROUP, key);
		}
		else
		{
			configManager.setConfiguration(RancourClanConfig.GROUP, key, value);
		}
	}
}
