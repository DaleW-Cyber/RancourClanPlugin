package com.rancour.clan.services;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;
import com.rancour.clan.models.Team;

public final class TeamReadyNotifier
{
	private final SeenTeamReadyStore store;
	private final Consumer<String> chatSink;
	private final BooleanSupplier enabled;

	public TeamReadyNotifier(SeenTeamReadyStore store, Consumer<String> chatSink, BooleanSupplier enabled)
	{
		this.store = store;
		this.chatSink = chatSink;
		this.enabled = enabled;
	}

	public void notifyReadyTeams(List<Team> teams)
	{
		for (Team team : teams)
		{
			if (!team.isJoined() || team.getCurrentMembers() < team.getCapacity() || store.isSeen(team.getId()))
			{
				continue;
			}
			store.markSeen(team.getId());
			if (enabled.getAsBoolean())
			{
				chatSink.accept("[Rancour] Your team is ready: " + team.getActivity() + " on world " + team.getWorld() + ".");
			}
		}
	}
}
