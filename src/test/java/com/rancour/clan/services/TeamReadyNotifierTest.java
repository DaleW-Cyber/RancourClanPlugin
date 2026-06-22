package com.rancour.clan.services;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import com.rancour.clan.models.Team;

public class TeamReadyNotifierTest
{
	@Test
	public void notifiesJoinedFullTeamOnlyOnce()
	{
		MemoryStore store = new MemoryStore();
		List<String> messages = new ArrayList<>();
		TeamReadyNotifier notifier = new TeamReadyNotifier(store, messages::add);
		Team team = new Team("team-1", "Nex", "Mutable", Collections.emptyList(), 2, 2, 420,
			true, "open", false, Collections.emptyList(), true,
			java.util.Arrays.asList("Mutable", "Beta"), "now", "later", "now", null, null, "Quick trip");

		notifier.notifyReadyTeams(Collections.singletonList(team));
		notifier.notifyReadyTeams(Collections.singletonList(team));

		assertEquals(1, messages.size());
		assertEquals("[Rancour] Your team is ready: Nex on world 420.", messages.get(0));
	}

	@Test
	public void ignoresTeamsThePlayerHasNotJoined()
	{
		List<String> messages = new ArrayList<>();
		TeamReadyNotifier notifier = new TeamReadyNotifier(new MemoryStore(), messages::add);
		Team team = new Team("team-1", "Nex", "Mutable", Collections.emptyList(), 2, 2, 420,
			true, "open", false, Collections.emptyList(), false,
			java.util.Arrays.asList("Mutable", "Beta"), "now", "later", "now", null, null, "");

		notifier.notifyReadyTeams(Collections.singletonList(team));

		assertEquals(0, messages.size());
	}

	private static final class MemoryStore implements SeenTeamReadyStore
	{
		private final Set<String> seen = new HashSet<>();

		@Override public boolean isSeen(String teamId) { return seen.contains(teamId); }
		@Override public void markSeen(String teamId) { seen.add(teamId); }
	}
}
