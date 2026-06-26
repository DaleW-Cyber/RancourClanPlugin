package com.rancour.clan.services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamCreateRequest;
import com.rancour.clan.models.TeamEditRequest;

public interface TeamService
{
	CompletionStage<List<Team>> loadTeams();
	CompletionStage<Team> create(TeamCreateRequest request);
	CompletionStage<Team> edit(String teamId, TeamEditRequest request);
	CompletionStage<ActionResult> join(String teamId);
	CompletionStage<ActionResult> leave(String teamId);
	CompletionStage<ActionResult> close(String teamId);
}
