package com.rancour.clan.config;

public enum AnnouncementPriority
{
	NORMAL(0),
	HIGH(1),
	URGENT(2);

	private final int level;

	AnnouncementPriority(int level)
	{
		this.level = level;
	}

	public boolean includes(String priority)
	{
		return levelOf(priority) >= level;
	}

	private static int levelOf(String priority)
	{
		if (priority == null)
		{
			return 0;
		}
		try
		{
			return valueOf(priority.trim().toUpperCase()).level;
		}
		catch (IllegalArgumentException error)
		{
			return 0;
		}
	}

	@Override
	public String toString()
	{
		return name().toLowerCase();
	}
}
