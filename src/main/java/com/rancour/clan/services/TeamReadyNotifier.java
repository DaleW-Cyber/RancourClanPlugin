package com.rancour.clan.services;

import java.util.List;
import java.util.function.Consumer;
import com.rancour.clan.models.Team;

public final class TeamReadyNotifier
{
	private final SeenTeamReadyStore store;
	private final Consumer<String> chatSink;

	public TeamReadyNotifier(SeenTeamReadyStore store, Consumer<String> chatSink)
	{
		this.store = store;
		this.chatSink = chatSink;
	}

	public void notifyReadyTeams(List<Team> teams)
	{
		for (Team team : teams)
		{
			if (!team.isJoined() || team.getCurrentMembers() < team.getCapacity() || store.isSeen(team.getId()))
			{
				continue;
			}
			chatSink.accept("[Rancour] Your team is ready: " + team.getActivity() + " on world " + team.getWorld() + ".");
			store.markSeen(team.getId());
		}
	}
}
