package com.rancour.clan.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.Test;
import com.rancour.clan.api.MockClanApiClient;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.ApiHealth;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.models.DropCandidate;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.PluginSettings;
import com.rancour.clan.models.VerificationStartResponse;
import com.rancour.clan.models.VerificationStatus;
import com.rancour.clan.services.ApiServices;
import com.rancour.clan.services.EventService;
import com.rancour.clan.services.InMemorySessionStore;
import com.rancour.clan.services.VerificationService;

public class RancourClanPanelTest
{
	@Test
	public void createsNavigationForEveryMvpPage()
	{
		MockClanApiClient api = new MockClanApiClient();
		VerificationService verification = ApiServices.verification(api, new InMemorySessionStore());
		RancourClanPanel panel = new RancourClanPanel(
			verification,
			ApiServices.announcements(api, verification),
			ApiServices.events(api, verification),
			ApiServices.drops(api, verification),
			ApiServices.teams(api, verification),
			ApiServices.staff(api, verification),
			api::fetchSettings,
			true);

		Set<String> buttons = new HashSet<>();
		collectButtonLabels(panel, buttons);
		assertTrue(buttons.contains("Verify"));
		assertTrue(buttons.contains("News"));
		assertTrue(buttons.contains("Events"));
		assertTrue(buttons.contains("Drops"));
		assertTrue(buttons.contains("Teams"));
		assertTrue(buttons.contains("Staff"));
		assertTrue(buttons.contains("Test API"));
	}

	@Test
	public void staffButtonFollowsVerifiedApiProfile() throws Exception
	{
		assertFalse(panelFor(null).isStaffButtonVisible());
		assertFalse(panelFor(profile(false)).isStaffButtonVisible());
		assertTrue(panelFor(profile(true)).isStaffButtonVisible());
	}

	@Test
	public void dropsButtonFollowsBackendSetting() throws Exception
	{
		RancourClanPanel panel = panelFor(profile(true), new PluginSettings(false, java.util.Collections.singletonList("Twisted bow")));

		assertFalse(panel.isDropsButtonVisible());
		assertFalse(panel.acceptsDropCandidate(candidate("Twisted bow")));
	}

	@Test
	public void dropCandidatesMustBeApprovedBySettingsCatalogue() throws Exception
	{
		RancourClanPanel panel = panelFor(profile(true), new PluginSettings(true, java.util.Collections.singletonList("Twisted bow")));

		assertTrue(panel.isDropsButtonVisible());
		assertTrue(panel.acceptsDropCandidate(candidate("Twisted bow")));
		assertFalse(panel.acceptsDropCandidate(candidate("Coins")));
	}

	@Test
	public void eventsPanelIsReadOnly() throws Exception
	{
		EventsPanel panel = new EventsPanel(new EventService()
		{
			@Override public CompletionStage<java.util.List<ClanEvent>> loadEvents()
			{
				return CompletableFuture.completedFuture(java.util.Collections.singletonList(
					new ClanEvent("event", "Raid night", "2026-06-22T20:00:00Z", "Bring supplies",
						"Host", "open", 3, false, "member", java.util.Collections.emptyList(), null)));
			}

			@Override public CompletionStage<ActionResult> join(String eventId) { throw new AssertionError("RuneLite events are read-only"); }
			@Override public CompletionStage<ActionResult> leave(String eventId) { throw new AssertionError("RuneLite events are read-only"); }
		});
		SwingUtilities.invokeAndWait(() -> { });

		Set<String> buttons = new HashSet<>();
		collectButtonLabels(panel, buttons);
		assertTrue(buttons.contains("Refresh"));
		assertFalse(buttons.contains("Join"));
		assertFalse(buttons.contains("Leave"));
		assertFalse(allText(panel).contains("Host"));
		assertFalse(allText(panel).contains("Status"));
	}

	private static RancourClanPanel panelFor(MemberProfile profile) throws Exception
	{
		return panelFor(profile, new PluginSettings(true, java.util.Arrays.asList("Twisted bow", "Dexterous prayer scroll")));
	}

	private static RancourClanPanel panelFor(MemberProfile profile, PluginSettings settings) throws Exception
	{
		MockClanApiClient api = new MockClanApiClient();
		VerificationService verification = new FixedVerificationService(profile);
		RancourClanPanel panel = new RancourClanPanel(
			verification,
			ApiServices.announcements(api, verification),
			ApiServices.events(api, verification),
			ApiServices.drops(api, verification),
			ApiServices.teams(api, verification),
			ApiServices.staff(api, verification),
			() -> CompletableFuture.completedFuture(settings),
			true);
		SwingUtilities.invokeAndWait(() -> { });
		return panel;
	}

	private static DropCandidate candidate(String item)
	{
		return new DropCandidate(item, "Game chat", "RSN", "2026-06-22T18:30:00Z", "chat_message");
	}

	private static MemberProfile profile(boolean staff)
	{
		return new MemberProfile("Discord", "RSN", "Member", staff, "later", "now");
	}

	private static final class FixedVerificationService implements VerificationService
	{
		private final MemberProfile profile;
		private Consumer<MemberProfile> listener;

		private FixedVerificationService(MemberProfile profile) { this.profile = profile; }
		@Override public CompletionStage<ApiHealth> testConnection() { return CompletableFuture.completedFuture(new ApiHealth("ok")); }
		@Override public CompletionStage<VerificationStartResponse> generateLinkCode() { return CompletableFuture.completedFuture(null); }
		@Override public CompletionStage<VerificationStatus> refreshStatus()
		{
			if (listener != null) { listener.accept(profile); }
			return CompletableFuture.completedFuture(new VerificationStatus(profile == null ? "pending" : "verified", null, profile, "later", "now"));
		}
		@Override public MemberProfile getCurrentProfile() { return profile; }
		@Override public boolean isVerified() { return profile != null; }
		@Override public String getSessionToken() { return profile == null ? "" : "token"; }
		@Override public void addProfileListener(Consumer<MemberProfile> listener) { this.listener = listener; }
		@Override public void clearSession() { }
	}

	private static void collectButtonLabels(Container container, Set<String> labels)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof JButton)
			{
				labels.add(((JButton) component).getText());
			}
			if (component instanceof Container)
			{
				collectButtonLabels((Container) component, labels);
			}
		}
	}

	private static String allText(Container container)
	{
		StringBuilder text = new StringBuilder();
		for (Component component : container.getComponents())
		{
			if (component instanceof javax.swing.JTextArea)
			{
				text.append(((javax.swing.JTextArea) component).getText()).append('\n');
			}
			if (component instanceof javax.swing.AbstractButton)
			{
				text.append(((javax.swing.AbstractButton) component).getText()).append('\n');
			}
			if (component instanceof Container)
			{
				text.append(allText((Container) component));
			}
		}
		return text.toString();
	}
}
