package com.rancour.clan.api;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.DropSubmission;
import com.rancour.clan.models.DropSubmissionResult;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.StaffDropSubmission;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.VerificationStartResponse;
import com.rancour.clan.models.VerificationStatus;

public final class MockClanApiClient implements ClanApiClient
{
	private final MemberProfile profile = new MemberProfile("Mock Discord User", "Mock RSN", "Member", true,
		"Mock session", Instant.now().toString());

	@Override public CompletionStage<VerificationStartResponse> startVerification() { return done(new VerificationStartResponse("MOCK-123", "mock-verification", "10 minutes")); }
	@Override public CompletionStage<VerificationStatus> fetchVerificationStatus(String id, String token) { return done(new VerificationStatus("verified", "mock-session-token", profile, profile.getExpiresAt(), profile.getLastCheckedAt())); }
	@Override public CompletionStage<MemberProfile> fetchProfile(String token) { return done(profile); }
	@Override public CompletionStage<List<Announcement>> fetchAnnouncements(String token) { return done(Arrays.asList(new Announcement("mock-news", "Mock announcement", "This is local mock mode data.", "normal", Instant.now().toString(), "No expiry", "Mock Staff", false))); }
	@Override public CompletionStage<List<ClanEvent>> fetchEvents(String token) { return done(Arrays.asList(new ClanEvent("mock-event", "Mock PvM Night", Instant.now().toString(), "Local mock event", "Mock Host", "open", 3, false))); }
	@Override public CompletionStage<ActionResult> joinEvent(String id, String token) { return ok("Joined mock event"); }
	@Override public CompletionStage<ActionResult> leaveEvent(String id, String token) { return ok("Left mock event"); }
	@Override public CompletionStage<DropSubmissionResult> submitDrop(DropSubmission submission, String token) { return done(new DropSubmissionResult("mock-drop", "pending", "Mock drop queued")); }
	@Override public CompletionStage<List<Team>> fetchTeams(String token) { return done(Arrays.asList(new Team("mock-team", "Theatre of Blood", "Mock Host", Arrays.asList("Any role"), 2, 5, 416, true, "open", true, Arrays.asList("staff-hosted", "learner"), false))); }
	@Override public CompletionStage<ActionResult> joinTeam(String id, String token) { return ok("Joined mock team"); }
	@Override public CompletionStage<ActionResult> leaveTeam(String id, String token) { return ok("Left mock team"); }
	@Override public CompletionStage<List<StaffDropSubmission>> fetchPendingDrops(String token) { return done(Collections.singletonList(new StaffDropSubmission("mock-pending", "Mock item", "Mock boss", "Mock RSN", Instant.now().toString(), "pending"))); }
	@Override public CompletionStage<ActionResult> approveDrop(String id, String token) { return ok("Approved mock submission"); }
	@Override public CompletionStage<ActionResult> rejectDrop(String id, String token) { return ok("Rejected mock submission"); }
	@Override public CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request, String token) { return done(new Announcement("mock-created", request.getTitle(), request.getMessage(), request.getPriority(), Instant.now().toString(), request.getExpiresAt(), "Mock Staff", false)); }
	@Override public CompletionStage<ActionResult> refreshEventCache(String token) { return ok("Mock event cache refreshed"); }
	@Override public CompletionStage<ActionResult> closeTeam(String id, String token) { return ok("Mock team closed"); }
	@Override public CompletionStage<ActionResult> lockTeam(String id, String token) { return ok("Mock team locked"); }

	private static CompletionStage<ActionResult> ok(String message) { return done(new ActionResult(true, message)); }
	private static <T> CompletionStage<T> done(T value) { return CompletableFuture.completedFuture(value); }
}
