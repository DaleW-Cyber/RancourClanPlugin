package com.rancour.clan.services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Team;

public interface TeamService
{
	CompletionStage<List<Team>> loadTeams();
	CompletionStage<ActionResult> join(String teamId);
	CompletionStage<ActionResult> leave(String teamId);
}
