package com.rancour.clan.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
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
			() -> api.fetchSettings("session"),
			true);

		Set<String> buttons = new HashSet<>();
		collectButtonLabels(panel, buttons);
		assertTrue(buttons.contains("Verify"));
		assertTrue(buttons.contains("News"));
		assertTrue(buttons.contains("Events"));
		assertTrue(buttons.contains("Drops"));
		assertTrue(buttons.contains("Teams"));
		assertTrue(buttons.contains("Create Team"));
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
	public void staffButtonRequiresSessionToken() throws Exception
	{
		assertFalse(panelFor(profile(true), new PluginSettings(true, java.util.Collections.emptyList()), "").isStaffButtonVisible());
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
	public void verifiedUserDefaultsToNewsPage() throws Exception
	{
		assertEquals("announcements", panelFor(profile(false)).currentPage());
	}

	@Test
	public void unverifiedUserDefaultsToVerifyPage() throws Exception
	{
		assertEquals("verification", panelFor(null).currentPage());
	}

	@Test
	public void successfulVerificationNavigatesToNewsPage() throws Exception
	{
		MutableVerificationService verification = new MutableVerificationService();
		MockClanApiClient api = new MockClanApiClient();
		RancourClanPanel panel = new RancourClanPanel(
			verification,
			ApiServices.announcements(api, verification),
			ApiServices.events(api, verification),
			ApiServices.drops(api, verification),
			ApiServices.teams(api, verification),
			ApiServices.staff(api, verification),
			() -> api.fetchSettings("session"),
			() -> "RSN");
		SwingUtilities.invokeAndWait(() -> { });

		assertEquals("verification", panel.currentPage());
		verification.set(profile(false), "session");
		SwingUtilities.invokeAndWait(() -> { });

		assertEquals("announcements", panel.currentPage());
	}

	@Test
	public void refreshDoesNotOverrideManualTabSelection() throws Exception
	{
		RancourClanPanel panel = panelFor(profile(false));
		button(panel, "Teams").doClick();
		SwingUtilities.invokeAndWait(() -> { });

		assertEquals("teams", panel.currentPage());
		assertTrue(panel.hasUserSelectedPage());
		panel.refreshAll();
		SwingUtilities.invokeAndWait(() -> { });

		assertEquals("teams", panel.currentPage());
	}

	@Test
	public void eventsPanelIsReadOnly() throws Exception
	{
		EventsPanel panel = eventsPanelFor(event("Raid night", "member"));
		SwingUtilities.invokeAndWait(() -> { });

		Set<String> buttons = new HashSet<>();
		collectButtonLabels(panel, buttons);
		assertTrue(buttons.contains("Refresh"));
		assertFalse(buttons.contains("Join"));
		assertFalse(buttons.contains("Leave"));
		assertFalse(allText(panel).contains("Host"));
		assertFalse(allText(panel).contains("Status"));
		assertFalse(allText(panel).contains("Tags"));
	}

	@Test
	public void eventsPanelShowsStaffBadgeForStaffVisibility() throws Exception
	{
		EventsPanel panel = eventsPanelFor(event("Staff meeting", "staff"));
		SwingUtilities.invokeAndWait(() -> { });

		String text = allText(panel);
		assertTrue(text.contains("STAFF EVENT"));
		assertFalse(text.contains("visibility=staff"));
		assertFalse(text.contains("123456789"));
	}

	@Test
	public void eventsPanelShowsRestrictedBadgeForRestrictedVisibility() throws Exception
	{
		EventsPanel panel = eventsPanelFor(event("Restricted raid", "restricted"));
		SwingUtilities.invokeAndWait(() -> { });

		String text = allText(panel);
		assertTrue(text.contains("RESTRICTED"));
		assertFalse(text.contains("visibility=restricted"));
		assertFalse(text.contains("123456789"));
	}

	@Test
	public void eventsPanelDoesNotBadgeNormalEvents() throws Exception
	{
		EventsPanel panel = eventsPanelFor(event("Clan mass", "member"));
		SwingUtilities.invokeAndWait(() -> { });

		String text = allText(panel);
		assertFalse(text.contains("STAFF EVENT"));
		assertFalse(text.contains("RESTRICTED"));
	}

	private static RancourClanPanel panelFor(MemberProfile profile) throws Exception
	{
		return panelFor(profile, new PluginSettings(true, java.util.Arrays.asList("Twisted bow", "Dexterous prayer scroll")));
	}

	private static RancourClanPanel panelFor(MemberProfile profile, PluginSettings settings) throws Exception
	{
		return panelFor(profile, settings, profile == null ? "" : "token");
	}

	private static RancourClanPanel panelFor(MemberProfile profile, PluginSettings settings, String token) throws Exception
	{
		MockClanApiClient api = new MockClanApiClient();
		VerificationService verification = new FixedVerificationService(profile, token);
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

	private static ClanEvent event(String name, String visibility)
	{
		return new ClanEvent("event", name, "2026-06-22T20:00:00Z", "Bring supplies",
			"Host", "open", 3, false, visibility, java.util.Collections.singletonList("123456789"), null);
	}

	private static EventsPanel eventsPanelFor(ClanEvent event)
	{
		return new EventsPanel(new EventService()
		{
			@Override public CompletionStage<java.util.List<ClanEvent>> loadEvents()
			{
				return CompletableFuture.completedFuture(java.util.Collections.singletonList(event));
			}

			@Override public CompletionStage<ActionResult> join(String eventId) { throw new AssertionError("RuneLite events are read-only"); }
			@Override public CompletionStage<ActionResult> leave(String eventId) { throw new AssertionError("RuneLite events are read-only"); }
		});
	}

	private static final class FixedVerificationService implements VerificationService
	{
		private final MemberProfile profile;
		private final String token;
		private Consumer<MemberProfile> listener;

		private FixedVerificationService(MemberProfile profile) { this(profile, profile == null ? "" : "token"); }
		private FixedVerificationService(MemberProfile profile, String token) { this.profile = profile; this.token = token; }
		@Override public CompletionStage<ApiHealth> testConnection() { return CompletableFuture.completedFuture(new ApiHealth("ok")); }
		@Override public CompletionStage<VerificationStartResponse> generateLinkCode() { return CompletableFuture.completedFuture(null); }
		@Override public CompletionStage<VerificationStatus> refreshStatus()
		{
			if (listener != null) { listener.accept(profile); }
			return CompletableFuture.completedFuture(new VerificationStatus(profile == null ? "pending" : "verified", null, profile, "later", "now"));
		}
		@Override public MemberProfile getCurrentProfile() { return profile; }
		@Override public boolean isVerified() { return profile != null; }
		@Override public String getSessionToken() { return token; }
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

	private static JButton button(Container container, String label)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof JButton && label.equals(((JButton) component).getText()))
			{
				return (JButton) component;
			}
			if (component instanceof Container)
			{
				try
				{
					return button((Container) component, label);
				}
				catch (AssertionError ignored)
				{
					// Keep searching sibling containers.
				}
			}
		}
		throw new AssertionError("Button not found: " + label);
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

	private static final class MutableVerificationService implements VerificationService
	{
		private MemberProfile profile;
		private String token = "";
		private Consumer<MemberProfile> listener;

		private void set(MemberProfile profile, String token)
		{
			this.profile = profile;
			this.token = token;
			if (listener != null)
			{
				listener.accept(profile);
			}
		}

		@Override public CompletionStage<ApiHealth> testConnection() { return CompletableFuture.completedFuture(new ApiHealth("ok")); }
		@Override public CompletionStage<VerificationStartResponse> generateLinkCode() { return CompletableFuture.completedFuture(null); }
		@Override public CompletionStage<VerificationStatus> refreshStatus() { return CompletableFuture.completedFuture(new VerificationStatus(profile == null ? "pending" : "verified", null, profile, "later", "now")); }
		@Override public MemberProfile getCurrentProfile() { return profile; }
		@Override public boolean isVerified() { return profile != null; }
		@Override public String getSessionToken() { return token; }
		@Override public void addProfileListener(Consumer<MemberProfile> listener) { this.listener = listener; }
		@Override public void clearSession() { set(null, ""); }
	}
}
