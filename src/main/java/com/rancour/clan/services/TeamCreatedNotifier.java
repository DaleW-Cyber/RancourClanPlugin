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
			store.markSeen(team.getId());
			if (initialised && enabled.getAsBoolean() && team.shouldNotifyCurrentUser())
			{
				chatSink.accept("[Rancour] New team: " + team.getActivity() + " on world " + team.getWorld() + ".");
			}
		}
		initialised = true;
	}
}
