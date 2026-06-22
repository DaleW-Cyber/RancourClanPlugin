package com.rancour.clan.services;

import java.util.List;
import com.rancour.clan.models.ClanEvent;

public interface EventService
{
	List<ClanEvent> getUpcomingEvents();
}
