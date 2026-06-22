package com.rancour.clan.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.Test;
import com.rancour.clan.api.ApiException;
import com.rancour.clan.api.ClanApiClient;
import com.rancour.clan.api.MockClanApiClient;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.ApiHealth;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.DropSubmission;
import com.rancour.clan.models.DropSubmissionResult;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.PluginSettings;
import com.rancour.clan.models.StaffDropSubmission;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.VerificationStartResponse;
import com.rancour.clan.models.VerificationStatus;

public class VerificationServiceTest
{
	@Test
	public void storesPendingIdThenSessionToken() throws Exception
	{
		InMemorySessionStore sessions = new InMemorySessionStore();
		VerificationService service = ApiServices.verification(new MockClanApiClient(), sessions);

		service.generateLinkCode().toCompletableFuture().get();
		assertEquals("mock-verification", sessions.getPendingVerificationId());
		service.refreshStatus().toCompletableFuture().get();

		assertTrue(service.isVerified());
		assertEquals("mock-session-token", sessions.getSessionToken());
		assertEquals("", sessions.getPendingVerificationId());
		assertEquals("Mock RSN", service.getCurrentProfile().getRsn());
	}

	@Test
	public void profileRefreshWithoutNewSessionTokenKeepsStoredToken() throws Exception
	{
		InMemorySessionStore sessions = new InMemorySessionStore();
		sessions.setSessionToken("existing-token");
		VerificationService service = ApiServices.verification(new ProfileWithoutTokenApi(), sessions);

		service.refreshStatus().toCompletableFuture().get();

		assertEquals("existing-token", sessions.getSessionToken());
		assertTrue(service.isVerified());
		assertTrue(service.getCurrentProfile().isStaff());
	}

	@Test
	public void protectedActionWithProfileButMissingTokenShowsExpiredSessionMessage() throws Exception
	{
		VerificationService service = new VerificationService()
		{
			@Override public java.util.concurrent.CompletionStage<com.rancour.clan.models.ApiHealth> testConnection() { return null; }
			@Override public java.util.concurrent.CompletionStage<com.rancour.clan.models.VerificationStartResponse> generateLinkCode() { return null; }
			@Override public java.util.concurrent.CompletionStage<com.rancour.clan.models.VerificationStatus> refreshStatus() { return null; }
			@Override public com.rancour.clan.models.MemberProfile getCurrentProfile() { return new com.rancour.clan.models.MemberProfile("Discord", "RSN", "Member", false, "later", "now"); }
			@Override public boolean isVerified() { return false; }
			@Override public String getSessionToken() { return ""; }
			@Override public void addProfileListener(java.util.function.Consumer<com.rancour.clan.models.MemberProfile> listener) { }
			@Override public void clearSession() { }
		};

		try
		{
			ApiServices.events(new MockClanApiClient(), service).join("event").toCompletableFuture().get();
		}
		catch (ExecutionException error)
		{
			assertEquals("Verification session missing. Please refresh verification or link again.", error.getCause().getMessage());
			return;
		}
		throw new AssertionError("Expected event join to fail without a session token");
	}

	@Test
	public void protectedActionWithSessionTokenDoesNotRequireLoadedProfile() throws Exception
	{
		VerificationService service = new FakeVerificationService(null, "session-token");

		ActionResult result = ApiServices.events(new MockClanApiClient(), service).join("event").toCompletableFuture().get();

		assertEquals("Joined mock event", result.getMessage());
	}

	@Test
	public void expiredTokenClearsLocalSession() throws Exception
	{
		FakeVerificationService service = new FakeVerificationService(null, "expired-token");

		try
		{
			ApiServices.events(new ExpiredSessionApi(), service).join("event").toCompletableFuture().get();
		}
		catch (ExecutionException error)
		{
			assertTrue(service.cleared);
			assertEquals("Verification session expired. Please refresh verification or link again.", error.getCause().getMessage());
			return;
		}
		throw new AssertionError("Expected expired session failure");
	}

	@Test
	public void staffProfileWithValidTokenCanCreateAndDeleteAnnouncement() throws Exception
	{
		FakeVerificationService service = new FakeVerificationService(new MemberProfile("Discord", "RSN", "Admin", true, "later", "now"), "staff-token");
		StaffService staff = ApiServices.staff(new MockClanApiClient(), service);

		Announcement created = staff.createAnnouncement(new CreateAnnouncementRequest("Title", "Body", "normal", "later"))
			.toCompletableFuture().get();
		ActionResult deleted = staff.deleteAnnouncement(created.getId()).toCompletableFuture().get();

		assertEquals("Title", created.getTitle());
		assertEquals("Announcement deleted", deleted.getMessage());
	}

	@Test
	public void staffProfileWithValidTokenCanToggleDropsPanel() throws Exception
	{
		FakeVerificationService service = new FakeVerificationService(new MemberProfile("Discord", "RSN", "Admin", true, "later", "now"), "staff-token");
		PluginSettings settings = ApiServices.staff(new MockClanApiClient(), service)
			.setDropsPanelEnabled(false)
			.toCompletableFuture().get();

		assertTrue(!settings.isDropsPanelEnabled());
	}

	@Test
	public void staffProfileWithMissingTokenGetsStaffSessionMessage() throws Exception
	{
		FakeVerificationService service = new FakeVerificationService(new MemberProfile("Discord", "RSN", "Admin", true, "later", "now"), "");

		try
		{
			ApiServices.staff(new MockClanApiClient(), service)
				.createAnnouncement(new CreateAnnouncementRequest("Title", "Body", "normal", "later"))
				.toCompletableFuture().get();
		}
		catch (ExecutionException error)
		{
			assertEquals("Staff session missing. Refresh verification or link again.", error.getCause().getMessage());
			return;
		}
		throw new AssertionError("Expected staff action to fail without a session token");
	}

	@Test
	public void nonStaffCannotUseStaffActions() throws Exception
	{
		FakeVerificationService service = new FakeVerificationService(new MemberProfile("Discord", "RSN", "Member", false, "later", "now"), "member-token");

		try
		{
			ApiServices.staff(new MockClanApiClient(), service)
				.deleteAnnouncement("announcement")
				.toCompletableFuture().get();
		}
		catch (ExecutionException error)
		{
			assertEquals("Staff verification is required for this action", error.getCause().getMessage());
			return;
		}
		throw new AssertionError("Expected non-staff action to fail");
	}

	private static final class FakeVerificationService implements VerificationService
	{
		private final MemberProfile profile;
		private final String token;
		private boolean cleared;

		private FakeVerificationService(MemberProfile profile, String token)
		{
			this.profile = profile;
			this.token = token;
		}

		@Override public CompletionStage<ApiHealth> testConnection() { return null; }
		@Override public CompletionStage<VerificationStartResponse> generateLinkCode() { return null; }
		@Override public CompletionStage<VerificationStatus> refreshStatus() { return null; }
		@Override public MemberProfile getCurrentProfile() { return profile; }
		@Override public boolean isVerified() { return profile != null && token != null && !token.isEmpty(); }
		@Override public String getSessionToken() { return token; }
		@Override public void addProfileListener(java.util.function.Consumer<MemberProfile> listener) { }
		@Override public void clearSession() { cleared = true; }
	}

	private static final class ExpiredSessionApi implements ClanApiClient
	{
		@Override public CompletionStage<ApiHealth> health() { return failed(); }
		@Override public CompletionStage<VerificationStartResponse> startVerification() { return failed(); }
		@Override public CompletionStage<VerificationStatus> fetchVerificationStatus(String verificationId, String sessionToken) { return failed(); }
		@Override public CompletionStage<MemberProfile> fetchProfile(String sessionToken) { return failed(); }
		@Override public CompletionStage<PluginSettings> fetchSettings() { return failed(); }
		@Override public CompletionStage<java.util.List<Announcement>> fetchAnnouncements(String sessionToken) { return failed(); }
		@Override public CompletionStage<java.util.List<ClanEvent>> fetchEvents(String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> joinEvent(String eventId, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> leaveEvent(String eventId, String sessionToken) { return failed(); }
		@Override public CompletionStage<DropSubmissionResult> submitDrop(DropSubmission submission, String sessionToken) { return failed(); }
		@Override public CompletionStage<java.util.List<Team>> fetchTeams(String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> joinTeam(String teamId, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> leaveTeam(String teamId, String sessionToken) { return failed(); }
		@Override public CompletionStage<java.util.List<StaffDropSubmission>> fetchPendingDrops(String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> approveDrop(String submissionId, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> rejectDrop(String submissionId, String sessionToken) { return failed(); }
		@Override public CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> deleteAnnouncement(String announcementId, String sessionToken) { return failed(); }
		@Override public CompletionStage<PluginSettings> setDropsPanelEnabled(boolean enabled, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> refreshEventCache(String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> closeTeam(String teamId, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> lockTeam(String teamId, String sessionToken) { return failed(); }

		private static <T> CompletionStage<T> failed()
		{
			CompletableFuture<T> future = new CompletableFuture<>();
			future.completeExceptionally(new ApiException("Verification session expired. Please refresh verification or link again.", 401));
			return future;
		}
	}

	private static final class ProfileWithoutTokenApi implements ClanApiClient
	{
		private final MemberProfile profile = new MemberProfile("Dale", "Mutable", "Server Admin", true, "2026-06-29T18:30:00Z", "2026-06-22T18:30:00Z");

		@Override public CompletionStage<ApiHealth> health() { return failed(); }
		@Override public CompletionStage<VerificationStartResponse> startVerification() { return failed(); }
		@Override public CompletionStage<VerificationStatus> fetchVerificationStatus(String verificationId, String sessionToken)
		{
			return CompletableFuture.completedFuture(new VerificationStatus("verified", null, profile, profile.getExpiresAt(), profile.getLastCheckedAt()));
		}
		@Override public CompletionStage<MemberProfile> fetchProfile(String sessionToken) { return CompletableFuture.completedFuture(profile); }
		@Override public CompletionStage<PluginSettings> fetchSettings() { return failed(); }
		@Override public CompletionStage<java.util.List<Announcement>> fetchAnnouncements(String sessionToken) { return failed(); }
		@Override public CompletionStage<java.util.List<ClanEvent>> fetchEvents(String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> joinEvent(String eventId, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> leaveEvent(String eventId, String sessionToken) { return failed(); }
		@Override public CompletionStage<DropSubmissionResult> submitDrop(DropSubmission submission, String sessionToken) { return failed(); }
		@Override public CompletionStage<java.util.List<Team>> fetchTeams(String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> joinTeam(String teamId, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> leaveTeam(String teamId, String sessionToken) { return failed(); }
		@Override public CompletionStage<java.util.List<StaffDropSubmission>> fetchPendingDrops(String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> approveDrop(String submissionId, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> rejectDrop(String submissionId, String sessionToken) { return failed(); }
		@Override public CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> deleteAnnouncement(String announcementId, String sessionToken) { return failed(); }
		@Override public CompletionStage<PluginSettings> setDropsPanelEnabled(boolean enabled, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> refreshEventCache(String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> closeTeam(String teamId, String sessionToken) { return failed(); }
		@Override public CompletionStage<ActionResult> lockTeam(String teamId, String sessionToken) { return failed(); }

		private static <T> CompletionStage<T> failed()
		{
			CompletableFuture<T> future = new CompletableFuture<>();
			future.completeExceptionally(new AssertionError("Unexpected API call"));
			return future;
		}
	}
}
