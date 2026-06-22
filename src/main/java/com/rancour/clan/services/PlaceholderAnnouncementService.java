package com.rancour.clan.services;

import java.util.Arrays;
import java.util.List;
import com.rancour.clan.models.Announcement;

public final class PlaceholderAnnouncementService implements AnnouncementService
{
	@Override
	public List<Announcement> getAnnouncements()
	{
		return Arrays.asList(
			new Announcement("Welcome to Rancour", "Clan announcements will appear here when the REST API is connected.", "Pinned"),
			new Announcement("Phase 1 preview", "This feed currently uses local placeholder content.", "Development")
		);
	}
}
