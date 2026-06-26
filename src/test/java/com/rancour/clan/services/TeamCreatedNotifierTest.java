package com.rancour.clan.services;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import com.rancour.clan.models.Team;

public class TeamCreatedNotifierTest
{
	@Test
	public void notifiesOnlyWhenApiAllowsCurrentUser()
	{
		MemoryStore store = new MemoryStore();
		List<String> messages = new ArrayList<>();
		TeamCreatedNotifier notifier = new TeamCreatedNotifier(store, messages::add, () -> true);
		Team initial = team("team-1", true);
		Team hidden = team("team-2", false);
		Team visible = team("team-3", true);

		notifier.notifyNewTeams(Collections.singletonList(initial));
		notifier.notifyNewTeams(java.util.Arrays.asList(hidden, visible));

		assertEquals(1, messages.size());
		assertEquals("[Rancour] New team: Nex on world 420.", messages.get(0));
	}

	private static Team team(String id, Boolean notifyCurrentUser)
	{
		return new Team(id, "Nex", "Mutable", Collections.emptyList(), 1, 5, 420,
			true, "open", false, Collections.emptyList(), false, Collections.singletonList("Mutable"),
			"now", "later", null, null, null, "", notifyCurrentUser);
	}

	private static final class MemoryStore implements SeenTeamReadyStore
	{
		private final Set<String> seen = new HashSet<>();

		@Override public boolean isSeen(String teamId) { return seen.contains(teamId); }
		@Override public void markSeen(String teamId) { seen.add(teamId); }
	}
}
