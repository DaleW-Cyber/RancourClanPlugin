package com.rancour.clan.services;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import com.rancour.clan.models.Team;

public final class TeamCreatedNotifier
{
	private static final Duration INITIAL_NOTIFICATION_GRACE = Duration.ofMinutes(5);

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
				if (enabled.getAsBoolean() && isFresh(team))
				{
					chatSink.accept("[Rancour] New team formed: " + team.getActivity() + " - Host: " + team.getHost());
				}
			}
		}
		initialised = true;
	}

	private static boolean isFresh(Team team)
	{
		try
		{
			OffsetDateTime createdAt = OffsetDateTime.parse(team.getCreatedAt());
			return createdAt.plus(INITIAL_NOTIFICATION_GRACE).isAfter(OffsetDateTime.now());
		}
		catch (DateTimeParseException | NullPointerException ex)
		{
			return false;
		}
	}
}
