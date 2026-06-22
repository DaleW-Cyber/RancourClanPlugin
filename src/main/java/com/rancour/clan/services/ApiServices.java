package com.rancour.clan.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.rancour.clan.api.ClanApiClient;
import com.rancour.clan.api.ApiException;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.ApiHealth;
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

public final class ApiServices
{
	private ApiServices()
	{
	}

	public static VerificationService verification(ClanApiClient api, SessionStore sessions)
	{
		return new ApiVerificationService(api, sessions);
	}

	public static AnnouncementService announcements(ClanApiClient api, VerificationService verification)
	{
		return () -> api.fetchAnnouncements(verification.getSessionToken()).thenApply(items ->
		{
			if (items == null || verification.isVerified())
			{
				return items;
			}
			return items.stream().filter(item -> !item.isRestricted()).collect(Collectors.toList());
		});
	}

	public static EventService events(ClanApiClient api, VerificationService verification)
	{
		return new EventService()
		{
			@Override public CompletionStage<List<ClanEvent>> loadEvents() { return api.fetchEvents(verification.getSessionToken()); }
			@Override public CompletionStage<ActionResult> join(String id) { return withToken(verification, token -> api.joinEvent(id, token)); }
			@Override public CompletionStage<ActionResult> leave(String id) { return withToken(verification, token -> api.leaveEvent(id, token)); }
		};
	}

	public static DropService drops(ClanApiClient api, VerificationService verification)
	{
		return submission -> withToken(verification, token -> api.submitDrop(submission, token));
	}

	public static TeamService teams(ClanApiClient api, VerificationService verification)
	{
		return new TeamService()
		{
			@Override public CompletionStage<List<Team>> loadTeams() { return api.fetchTeams(verification.getSessionToken()); }
			@Override public CompletionStage<ActionResult> join(String id) { return withToken(verification, token -> api.joinTeam(id, token)); }
			@Override public CompletionStage<ActionResult> leave(String id) { return withToken(verification, token -> api.leaveTeam(id, token)); }
		};
	}

	public static StaffService staff(ClanApiClient api, VerificationService verification)
	{
		return new StaffService()
		{
			@Override public CompletionStage<List<StaffDropSubmission>> loadPendingDrops() { return withStaff(verification, token -> api.fetchPendingDrops(token)); }
			@Override public CompletionStage<ActionResult> approveDrop(String id) { return withStaff(verification, token -> api.approveDrop(id, token)); }
			@Override public CompletionStage<ActionResult> rejectDrop(String id) { return withStaff(verification, token -> api.rejectDrop(id, token)); }
			@Override public CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request) { return withStaff(verification, token -> api.createAnnouncement(request, token)); }
			@Override public CompletionStage<ActionResult> refreshEventCache() { return withStaff(verification, api::refreshEventCache); }
		};
	}

	private static <T> CompletionStage<T> withToken(VerificationService verification, AsyncCall<T> call)
	{
		if (!hasText(verification.getSessionToken()))
		{
			return failed("Verification session missing. Please refresh verification or link again.");
		}
		CompletionStage<T> result = call.invoke(verification.getSessionToken());
		result.whenComplete((value, error) ->
		{
			if (isUnauthorized(error))
			{
				verification.clearSession();
			}
		});
		return result;
	}

	private static <T> CompletionStage<T> withStaff(VerificationService verification, AsyncCall<T> call)
	{
		MemberProfile profile = verification.getCurrentProfile();
		if (!hasText(verification.getSessionToken()))
		{
			return failed("Verification session missing. Please refresh verification or link again.");
		}
		if (profile == null || !profile.isStaff())
		{
			return failed("Staff verification is required for this action");
		}
		CompletionStage<T> result = call.invoke(verification.getSessionToken());
		result.whenComplete((value, error) ->
		{
			if (isUnauthorized(error))
			{
				verification.clearSession();
			}
		});
		return result;
	}

	private static <T> CompletionStage<T> failed(String message)
	{
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(new IllegalStateException(message));
		return future;
	}

	private interface AsyncCall<T>
	{
		CompletionStage<T> invoke(String token);
	}

	private static boolean isUnauthorized(Throwable error)
	{
		Throwable current = error;
		while (current != null)
		{
			if (current instanceof ApiException && ((ApiException) current).getStatusCode() == 401)
			{
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private static final class ApiVerificationService implements VerificationService
	{
		private final ClanApiClient api;
		private final SessionStore sessions;
		private final List<Consumer<MemberProfile>> listeners = new CopyOnWriteArrayList<>();
		private volatile MemberProfile currentProfile;

		private ApiVerificationService(ClanApiClient api, SessionStore sessions)
		{
			this.api = api;
			this.sessions = sessions;
		}

		@Override
		public CompletionStage<ApiHealth> testConnection()
		{
			return api.health();
		}

		@Override
		public CompletionStage<VerificationStartResponse> generateLinkCode()
		{
			return api.startVerification().thenApply(response ->
			{
				sessions.setPendingVerificationId(response.getVerificationId());
				return response;
			});
		}

		@Override
		public CompletionStage<VerificationStatus> refreshStatus()
		{
			CompletionStage<VerificationStatus> result = api.fetchVerificationStatus(sessions.getPendingVerificationId(), sessions.getSessionToken())
				.thenCompose(status ->
				{
					if (hasText(status.getSessionToken()))
					{
						sessions.setSessionToken(status.getSessionToken());
						sessions.setPendingVerificationId("");
					}
					if (status.getProfile() != null)
					{
						updateProfile(status.getProfile());
						return CompletableFuture.completedFuture(status);
					}
					if (status.isVerified() && hasText(sessions.getSessionToken()))
					{
						return api.fetchProfile(sessions.getSessionToken()).thenApply(profile ->
						{
							updateProfile(profile);
							return new VerificationStatus(status.getState(), sessions.getSessionToken(), profile,
								status.getExpiresAt(), status.getLastCheckedAt());
						});
					}
					return CompletableFuture.completedFuture(status);
				});
			result.whenComplete((status, error) ->
			{
				if (isUnauthorized(error))
				{
					clearSession();
				}
			});
			return result;
		}

		@Override public MemberProfile getCurrentProfile() { return currentProfile; }
		@Override public boolean isVerified() { return currentProfile != null && hasText(sessions.getSessionToken()); }
		@Override public String getSessionToken() { return sessions.getSessionToken(); }
		@Override public void addProfileListener(Consumer<MemberProfile> listener) { listeners.add(listener); }
		@Override public void clearSession() { sessions.clear(); updateProfile(null); }

		private void updateProfile(MemberProfile profile)
		{
			currentProfile = profile;
			for (Consumer<MemberProfile> listener : listeners)
			{
				listener.accept(profile);
			}
		}
	}

	private static boolean hasText(String value)
	{
		return value != null && !value.trim().isEmpty();
	}
}
