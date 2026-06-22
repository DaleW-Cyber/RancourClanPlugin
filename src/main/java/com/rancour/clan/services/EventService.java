package com.rancour.clan.services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.ClanEvent;

public interface EventService
{
	CompletionStage<List<ClanEvent>> loadEvents();
	CompletionStage<ActionResult> join(String eventId);
	CompletionStage<ActionResult> leave(String eventId);
}
