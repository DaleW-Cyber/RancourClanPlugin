package com.rancour.clan.services;

import java.util.Arrays;
import java.util.List;
import com.rancour.clan.models.ClanEvent;

public final class PlaceholderEventService implements EventService
{
	@Override
	public List<ClanEvent> getUpcomingEvents()
	{
		return Arrays.asList(
			new ClanEvent("Clan PvM Night", "Friday, 20:00", "Placeholder event entry"),
			new ClanEvent("Learner Session", "Sunday, 18:00", "Signups will be added in a future phase")
		);
	}
}
