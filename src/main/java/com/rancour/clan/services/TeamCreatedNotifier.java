package com.rancour.clan.services;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import com.rancour.clan.models.Team;

public final class TeamCreatedNotifier
{
	private final SeenTeamReadyStore store;
	private final Consumer<String> chatSink;
	private final BooleanSupplier enabled;
	private boolean initialised;

	public TeamCreatedNotifier(SeenTeamReadyStore store, Consumer<String> chatSink, BooleanSupplier enabled)
	{
		this.store = store;
		this.chatSink = chatSink;
		this.enabled = enabled;
	}

	public void notifyNewTeams(List<Team> teams)
	{
		for (Team team : teams)
		{
			if (store.isSeen(team.getId()))
			{
				continue;
			}
			if (!team.shouldNotifyCurrentUser())
			{
				continue;
			}
			if (initialised)
			{
				store.markSeen(team.getId());
				if (enabled.getAsBoolean())
				{
					chatSink.accept("[Rancour] New team formed: " + team.getActivity() + " - Host: " + team.getHost());
				}
			}
			else
			{
				store.markSeen(team.getId());
			}
		}
		initialised = true;
	}
}
