package com.rancour.clan.services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamCreateRequest;

public final class NotifyingTeamService implements TeamService
{
	private final TeamService delegate;
	private final TeamReadyNotifier notifier;

	public NotifyingTeamService(TeamService delegate, TeamReadyNotifier notifier)
	{
		this.delegate = delegate;
		this.notifier = notifier;
	}

	@Override
	public CompletionStage<List<Team>> loadTeams()
	{
		return delegate.loadTeams().thenApply(teams ->
		{
			notifier.notifyReadyTeams(teams);
			return teams;
		});
	}

	@Override
	public CompletionStage<Team> create(TeamCreateRequest request)
	{
		return delegate.create(request);
	}

	@Override
	public CompletionStage<ActionResult> join(String id)
	{
		return delegate.join(id);
	}

	@Override
	public CompletionStage<ActionResult> leave(String id)
	{
		return delegate.leave(id);
	}
}
