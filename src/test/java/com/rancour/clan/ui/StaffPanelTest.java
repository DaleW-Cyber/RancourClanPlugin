package com.rancour.clan.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.EditAnnouncementRequest;
import com.rancour.clan.models.PluginSettings;
import com.rancour.clan.models.StaffDropSubmission;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamEditRequest;
import com.rancour.clan.services.StaffService;
import java.time.Duration;
import java.time.Instant;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractButton;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.junit.Test;

public class StaffPanelTest
{
	@Test
	public void createAnnouncementSubmitsTypedRequestAndRefreshesNews() throws Exception
	{
		RecordingStaffService service = new RecordingStaffService();
		AtomicInteger refreshes = new AtomicInteger();
		StaffPanel panel = new StaffPanel(service, refreshes::incrementAndGet);

		SwingUtilities.invokeAndWait(() ->
		{
			panel.showAnnouncementPage();
			panel.announcementTitle.setText("Clan update");
			panel.announcementMessage.setText("Body for the news page");
			panel.createAnnouncementButton.doClick();
		});
		SwingUtilities.invokeAndWait(() -> { });

		CreateAnnouncementRequest request = service.created.get();
		assertNotNull(request);
		assertEquals("Clan update", request.getTitle());
		assertEquals("Body for the news page", request.getMessage());
		assertEquals("normal", request.getPriority());
		Instant expiresAt = Instant.parse(request.getExpiresAt());
		assertTrue(Duration.between(Instant.now(), expiresAt).toMinutes() <= 61);
		assertEquals(1, refreshes.get());

		SwingUtilities.invokeAndWait(() ->
		{
			assertEquals("", panel.announcementTitle.getText());
			assertEquals("", panel.announcementMessage.getText());
		});
	}

	@Test
	public void createAnnouncementValidatesTitleAndBodyBeforeSubmitting() throws Exception
	{
		RecordingStaffService service = new RecordingStaffService();
		StaffPanel panel = new StaffPanel(service);

		SwingUtilities.invokeAndWait(() ->
		{
			panel.showAnnouncementPage();
			panel.createAnnouncementButton.doClick();
		});

		assertFalse(service.created.get() != null);
	}

	@Test
	public void staffMenuHidesBrokenOrDiscordOnlyTools() throws Exception
	{
		StaffPanel panel = new StaffPanel(new RecordingStaffService());
		SwingUtilities.invokeAndWait(() -> { });

		String text = allText(panel);
		assertTrue(text.contains("Staff"));
		assertTrue(text.contains("Announcements"));
		assertTrue(text.contains("Drops Panel"));
		assertTrue(text.contains("Teams"));
		assertFalse(text.contains("Pending Drops"));
		assertFalse(text.contains("Refresh Event Cache"));
		assertFalse(text.contains("Close Team"));
		assertFalse(text.contains("Lock Team"));
		assertFalse(hasDisabledButton(panel));
	}

	@Test
	public void dropsPanelShowsPluginApprovalMode() throws Exception
	{
		StaffPanel panel = new StaffPanel(new RecordingStaffService());
		SwingUtilities.invokeAndWait(() ->
		{
			panel.applySettings(new PluginSettings(true, "members", true, true, true, null,
				Collections.emptyList(), Collections.emptyList()));
			panel.showMenu();
			panel.dropsPanelButton.doClick();
		});

		String text = allText(panel);
		assertTrue(text.contains("Staff approval"));
		assertTrue(text.contains("Enabled"));
		assertTrue(text.contains("Auto-log Plugin Drops"));
	}

	@Test
	public void staffTeamsPageShowsMembersAndActions() throws Exception
	{
		RecordingStaffService service = new RecordingStaffService();
		service.teams.add(new Team("team-1", "Nex", "Dale", Collections.emptyList(), 2, 5, 505, true,
			"open", false, Collections.emptyList(), false, java.util.Arrays.asList("Dale", "Wolf"),
			"2026-06-22T18:30:00Z", "2026-06-22T20:30:00Z", null, null));
		StaffPanel panel = new StaffPanel(service);

		SwingUtilities.invokeAndWait(() ->
		{
			panel.showMenu();
			panel.teamsButton.doClick();
		});
		SwingUtilities.invokeAndWait(() -> { });

		String text = allText(panel);
		assertTrue(text.contains("Nex"));
		assertTrue(text.contains("Dale, Wolf"));
		assertTrue(text.contains("Edit"));
		assertTrue(text.contains("Close"));
	}

	@Test
	public void staffCanSeeDeleteButtonForCurrentAnnouncements() throws Exception
	{
		RecordingStaffService service = new RecordingStaffService();
		service.announcements.add(new Announcement("ann-1", "Update", "One line body",
			"normal", "now", "later", "Staff", false));
		StaffPanel panel = new StaffPanel(service);

		SwingUtilities.invokeAndWait(panel::showAnnouncementPage);
		SwingUtilities.invokeAndWait(() -> { });

		String text = allText(panel);
		assertTrue(text.contains("Existing Announcements"));
		assertTrue(text.contains("Update"));
		assertTrue(text.contains("One line body"));
		assertTrue(text.contains("Delete"));
	}

	@Test
	public void deleteAnnouncementRequiresConfirmationAndRefreshes() throws Exception
	{
		RecordingStaffService service = new RecordingStaffService();
		service.announcements.add(new Announcement("ann-1", "Update", "Body",
			"normal", "now", "later", "Staff", false));
		AtomicInteger refreshes = new AtomicInteger();
		AtomicReference<String> prompt = new AtomicReference<>();
		AtomicBoolean allow = new AtomicBoolean(false);
		StaffPanel panel = new StaffPanel(service, refreshes::incrementAndGet, message ->
		{
			prompt.set(message);
			return allow.get();
		});

		SwingUtilities.invokeAndWait(panel::showAnnouncementPage);
		SwingUtilities.invokeAndWait(() -> assertTrue(clickButton(panel, "Delete")));
		SwingUtilities.invokeAndWait(() -> { });
		assertEquals("Delete this announcement?", prompt.get());
		assertEquals(null, service.deleted.get());

		allow.set(true);
		SwingUtilities.invokeAndWait(() -> assertTrue(clickButton(panel, "Delete")));
		SwingUtilities.invokeAndWait(() -> { });
		SwingUtilities.invokeAndWait(() -> { });

		assertEquals("ann-1", service.deleted.get());
		assertEquals(1, refreshes.get());
		assertFalse(allText(panel).contains("Update"));
	}

	private static String allText(Container container)
	{
		StringBuilder text = new StringBuilder();
		for (Component component : container.getComponents())
		{
			if (component instanceof JTextArea)
			{
				text.append(((JTextArea) component).getText()).append('\n');
			}
			if (component instanceof javax.swing.JLabel)
			{
				text.append(((javax.swing.JLabel) component).getText()).append('\n');
			}
			if (component instanceof AbstractButton)
			{
				text.append(((AbstractButton) component).getText()).append('\n');
			}
			if (component instanceof Container)
			{
				text.append(allText((Container) component));
			}
		}
		return text.toString();
	}

	private static boolean hasDisabledButton(Container container)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof AbstractButton && !component.isEnabled())
			{
				return true;
			}
			if (component instanceof Container && hasDisabledButton((Container) component))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean clickButton(Container container, String label)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof AbstractButton && label.equals(((AbstractButton) component).getText()))
			{
				((AbstractButton) component).doClick();
				return true;
			}
			if (component instanceof Container)
			{
				if (clickButton((Container) component, label))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static final class RecordingStaffService implements StaffService
	{
		private final AtomicReference<CreateAnnouncementRequest> created = new AtomicReference<>();
		private final AtomicReference<String> deleted = new AtomicReference<>();
		private final List<Announcement> announcements = new ArrayList<>();
		private final List<Team> teams = new ArrayList<>();

		@Override
		public CompletionStage<List<Announcement>> loadAnnouncements()
		{
			return CompletableFuture.completedFuture(new ArrayList<>(announcements));
		}

		@Override
		public CompletionStage<List<StaffDropSubmission>> loadPendingDrops()
		{
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		@Override
		public CompletionStage<ActionResult> approveDrop(String submissionId)
		{
			return CompletableFuture.completedFuture(new ActionResult(true, "approved"));
		}

		@Override
		public CompletionStage<ActionResult> rejectDrop(String submissionId)
		{
			return CompletableFuture.completedFuture(new ActionResult(true, "rejected"));
		}

		@Override
		public CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request)
		{
			created.set(request);
			Announcement item = new Announcement("id", request.getTitle(), request.getMessage(),
				request.getPriority(), Instant.now().toString(), request.getExpiresAt(), "Staff", false);
			announcements.add(item);
			return CompletableFuture.completedFuture(item);
		}

		@Override
		public CompletionStage<Announcement> editAnnouncement(String announcementId, EditAnnouncementRequest request)
		{
			Announcement item = new Announcement(announcementId, request.getTitle(), request.getMessage(),
				request.getPriority(), Instant.now().toString(), request.getExpiresAt(), "Staff",
				Boolean.TRUE.equals(request.getRestricted()));
			announcements.removeIf(existing -> announcementId.equals(existing.getId()));
			announcements.add(item);
			return CompletableFuture.completedFuture(item);
		}

		@Override
		public CompletionStage<ActionResult> deleteAnnouncement(String announcementId)
		{
			deleted.set(announcementId);
			announcements.removeIf(item -> announcementId.equals(item.getId()));
			return CompletableFuture.completedFuture(new ActionResult(true, "Announcement deleted"));
		}

		@Override
		public CompletionStage<PluginSettings> setDropsPanelEnabled(boolean enabled)
		{
			return CompletableFuture.completedFuture(new PluginSettings(enabled, Collections.emptyList()));
		}

		@Override
		public CompletionStage<PluginSettings> setDropsAccessMode(String mode)
		{
			return CompletableFuture.completedFuture(new PluginSettings(true, mode, true, true, null,
				Collections.emptyList(), Collections.emptyList()));
		}

		@Override
		public CompletionStage<PluginSettings> setPluginDropsRequireStaffApproval(boolean requireApproval)
		{
			return CompletableFuture.completedFuture(new PluginSettings(true, "members", requireApproval,
				true, true, null, Collections.emptyList(), Collections.emptyList()));
		}

		@Override
		public CompletionStage<List<Team>> loadTeams()
		{
			return CompletableFuture.completedFuture(new ArrayList<>(teams));
		}

		@Override
		public CompletionStage<Team> editTeam(String teamId, TeamEditRequest request)
		{
			return CompletableFuture.completedFuture(new Team(teamId, request.getActivity(), "Dale",
				Collections.emptyList(), 1, request.getCapacity(), request.getWorld(),
				Boolean.TRUE.equals(request.getVoiceRequired()), request.getStatus(), false,
				request.getTags(), false));
		}

		@Override
		public CompletionStage<ActionResult> closeTeam(String teamId)
		{
			teams.removeIf(team -> teamId.equals(team.getId()));
			return CompletableFuture.completedFuture(new ActionResult(true, "Team closed"));
		}

		@Override
		public CompletionStage<ActionResult> refreshEventCache()
		{
			return CompletableFuture.completedFuture(new ActionResult(true, "refreshed"));
		}
	}
}
