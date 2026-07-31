package com.rancour.clan.ui;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.swing.SwingUtilities;
import org.junit.Test;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamCreateRequest;
import com.rancour.clan.models.TeamEditRequest;
import com.rancour.clan.services.TeamService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TeamsPanelTest
{
	@Test
	public void automaticRefreshDoesNotCloseOpenCreateForm() throws Exception
	{
		FakeTeamService service = new FakeTeamService();
		TeamsPanel panel = createPanel(service);

		SwingUtilities.invokeAndWait(() ->
		{
			panel.showCreateForm();
			panel.refreshIfIdle();
			assertTrue(panel.isFormOpen());
		});

		assertEquals("Only the constructor load should run while the form is open", 1, service.loadCalls);
	}

	@Test
	public void inFlightAutomaticRefreshDoesNotOverwriteNewForm() throws Exception
	{
		FakeTeamService service = new FakeTeamService();
		TeamsPanel panel = createPanel(service);
		CompletableFuture<List<Team>> pending = new CompletableFuture<>();
		service.nextLoad = pending;

		SwingUtilities.invokeAndWait(panel::refreshIfIdle);
		assertEquals(2, service.loadCalls);
		SwingUtilities.invokeAndWait(panel::showCreateForm);

		pending.complete(Collections.emptyList());
		flushEdt();

		assertTrue("The create form must survive a refresh that started before it opened", panel.isFormOpen());
	}

	private static TeamsPanel createPanel(FakeTeamService service) throws Exception
	{
		TeamsPanel[] panel = new TeamsPanel[1];
		SwingUtilities.invokeAndWait(() -> panel[0] = new TeamsPanel(service));
		flushEdt();
		return panel[0];
	}

	private static void flushEdt() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> { });
	}

	private static final class FakeTeamService implements TeamService
	{
		private volatile CompletableFuture<List<Team>> nextLoad;
		private int loadCalls;

		@Override
		public CompletionStage<List<Team>> loadTeams()
		{
			loadCalls++;
			CompletableFuture<List<Team>> pending = nextLoad;
			if (pending != null)
			{
				nextLoad = null;
				return pending;
			}
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		@Override
		public CompletionStage<Team> create(TeamCreateRequest request)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletionStage<Team> edit(String teamId, TeamEditRequest request)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletionStage<ActionResult> join(String teamId)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletionStage<ActionResult> leave(String teamId)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletionStage<ActionResult> close(String teamId)
		{
			return CompletableFuture.completedFuture(null);
		}
	}
}
