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
import com.rancour.clan.models.ApiHealth;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.VerificationStartResponse;
import com.rancour.clan.models.VerificationStatus;
import com.rancour.clan.services.ApiServices;
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
			true);

		Set<String> buttons = new HashSet<>();
		collectButtonLabels(panel, buttons);
		assertTrue(buttons.contains("Verify"));
		assertTrue(buttons.contains("News"));
		assertTrue(buttons.contains("Events"));
		assertTrue(buttons.contains("Drops"));
		assertTrue(buttons.contains("Teams"));
		assertTrue(buttons.contains("Staff"));
		assertTrue(buttons.contains("Test API Connection"));
	}

	@Test
	public void staffButtonFollowsVerifiedApiProfile() throws Exception
	{
		assertFalse(panelFor(null).isStaffButtonVisible());
		assertFalse(panelFor(profile(false)).isStaffButtonVisible());
		assertTrue(panelFor(profile(true)).isStaffButtonVisible());
	}

	private static RancourClanPanel panelFor(MemberProfile profile) throws Exception
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
			true);
		SwingUtilities.invokeAndWait(() -> { });
		return panel;
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
}
