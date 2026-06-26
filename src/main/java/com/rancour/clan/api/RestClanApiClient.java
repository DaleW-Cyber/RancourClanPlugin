package com.rancour.clan.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.regex.Pattern;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.ApiHealth;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.DropSubmission;
import com.rancour.clan.models.DropSubmissionResult;
import com.rancour.clan.models.EditAnnouncementRequest;
import com.rancour.clan.models.DropsPanelSettingRequest;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.PluginSettings;
import com.rancour.clan.models.StaffDropSubmission;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamActionRequest;
import com.rancour.clan.models.TeamCreateRequest;
import com.rancour.clan.models.TeamEditRequest;
import com.rancour.clan.models.VerificationStartResponse;
import com.rancour.clan.models.VerificationStatus;

@Slf4j
public final class RestClanApiClient implements ClanApiClient
{
	public static final String PRODUCTION_API_BASE_URL = "https://rancourdiscordbot-production.up.railway.app";
	private static final String API_BASE_URL_PROPERTY = "rancour.apiBaseUrl";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final int MAX_LOGGED_BODY_LENGTH = 2000;
	private static final Pattern JSON_SECRET = Pattern.compile(
		"(?i)(\\\"(?:sessionToken|token|secret|authorization)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")"
	);
	private static final Pattern BEARER_SECRET = Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._~+/-]+={0,2}");
	private static final Type ANNOUNCEMENT_LIST = new TypeToken<List<Announcement>>() { }.getType();
	private static final Type EVENT_LIST = new TypeToken<List<ClanEvent>>() { }.getType();
	private static final Type TEAM_LIST = new TypeToken<List<Team>>() { }.getType();
	private static final Type STAFF_DROP_LIST = new TypeToken<List<StaffDropSubmission>>() { }.getType();

	private final OkHttpClient httpClient;
	private final Gson gson;
	private final HttpUrl baseUrl;
	private final ApiException configurationError;

	public RestClanApiClient(OkHttpClient httpClient, Gson gson, String baseUrl)
	{
		this.httpClient = httpClient;
		this.gson = gson;
		HttpUrl parsed = HttpUrl.parse(normalizeBaseUrl(baseUrl));
		this.baseUrl = parsed == null ? HttpUrl.parse("http://invalid.local/") : parsed;
		this.configurationError = parsed == null ? new ApiException("Rancour API URL is invalid; check the local development override") : null;
	}

	public static String defaultBaseUrl()
	{
		String override = System.getProperty(API_BASE_URL_PROPERTY, "").trim();
		return hasText(override) ? override : PRODUCTION_API_BASE_URL;
	}

	@Override
	public CompletionStage<ApiHealth> health()
	{
		return get(url("health"), null, ApiHealth.class);
	}

	@Override
	public CompletionStage<VerificationStartResponse> startVerification()
	{
		HttpUrl target = url("plugin", "verification", "start");
		log.info("Rancour API verification start: POST {}", target);
		return post(target, new Object(), null, VerificationStartResponse.class);
	}

	@Override
	public CompletionStage<VerificationStatus> fetchVerificationStatus(String verificationId, String sessionToken)
	{
		HttpUrl.Builder builder = url("plugin", "verification", "status").newBuilder();
		if (hasText(verificationId))
		{
			builder.addQueryParameter("verificationId", verificationId);
		}
		return get(builder.build(), sessionToken, VerificationStatus.class);
	}

	@Override
	public CompletionStage<MemberProfile> fetchProfile(String sessionToken)
	{
		return get(url("plugin", "me"), sessionToken, MemberProfile.class);
	}

	@Override
	public CompletionStage<PluginSettings> fetchSettings()
	{
		return get(url("plugin", "settings"), null, PluginSettings.class);
	}

	@Override
	public CompletionStage<List<Announcement>> fetchAnnouncements(String sessionToken)
	{
		return get(url("plugin", "announcements"), sessionToken, ANNOUNCEMENT_LIST);
	}

	@Override
	public CompletionStage<List<ClanEvent>> fetchEvents(String sessionToken)
	{
		return get(url("plugin", "events"), sessionToken, EVENT_LIST);
	}

	@Override
	public CompletionStage<ActionResult> joinEvent(String eventId, String sessionToken)
	{
		return protectedPost(url("plugin", "events", eventId, "join"), new Object(), sessionToken, ActionResult.class);
	}

	@Override
	public CompletionStage<ActionResult> leaveEvent(String eventId, String sessionToken)
	{
		return protectedPost(url("plugin", "events", eventId, "leave"), new Object(), sessionToken, ActionResult.class);
	}

	@Override
	public CompletionStage<DropSubmissionResult> submitDrop(DropSubmission submission, String sessionToken)
	{
		return protectedPost(url("plugin", "drops"), submission, sessionToken, DropSubmissionResult.class);
	}

	@Override
	public CompletionStage<List<Team>> fetchTeams(String sessionToken)
	{
		return get(url("plugin", "teams"), sessionToken, TEAM_LIST);
	}

	@Override
	public CompletionStage<Team> createTeam(TeamCreateRequest request, String sessionToken)
	{
		return protectedPost(url("plugin", "teams"), request, sessionToken, Team.class);
	}

	@Override
	public CompletionStage<ActionResult> joinTeam(String teamId, String activeRsn, String sessionToken)
	{
		return protectedPost(url("plugin", "teams", teamId, "join"), new TeamActionRequest(activeRsn), sessionToken, ActionResult.class);
	}

	@Override
	public CompletionStage<ActionResult> leaveTeam(String teamId, String activeRsn, String sessionToken)
	{
		return protectedPost(url("plugin", "teams", teamId, "leave"), new TeamActionRequest(activeRsn), sessionToken, ActionResult.class);
	}

	@Override
	public CompletionStage<List<StaffDropSubmission>> fetchPendingDrops(String sessionToken)
	{
		return protectedGet(url("plugin", "staff", "drop-submissions"), sessionToken, STAFF_DROP_LIST);
	}

	@Override
	public CompletionStage<ActionResult> approveDrop(String submissionId, String sessionToken)
	{
		return protectedPost(url("plugin", "staff", "drop-submissions", submissionId, "approve"), new Object(), sessionToken, ActionResult.class);
	}

	@Override
	public CompletionStage<ActionResult> rejectDrop(String submissionId, String sessionToken)
	{
		return protectedPost(url("plugin", "staff", "drop-submissions", submissionId, "reject"), new Object(), sessionToken, ActionResult.class);
	}

	@Override
	public CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request, String sessionToken)
	{
		return protectedPost(url("plugin", "staff", "announcements"), request, sessionToken, Announcement.class);
	}

	@Override
	public CompletionStage<Announcement> editAnnouncement(String announcementId, EditAnnouncementRequest request, String sessionToken)
	{
		return protectedPatch(url("plugin", "staff", "announcements", announcementId), request, sessionToken, Announcement.class);
	}

	@Override
	public CompletionStage<ActionResult> deleteAnnouncement(String announcementId, String sessionToken)
	{
		return protectedDelete(url("plugin", "staff", "announcements", announcementId), sessionToken, ActionResult.class);
	}

	@Override
	public CompletionStage<PluginSettings> setDropsPanelEnabled(boolean enabled, String sessionToken)
	{
		HttpUrl endpoint = url("plugin", "staff", "settings", "drops-panel");
		log.info("Rancour API staff action request: action=dropsPanelToggle endpoint={} hasSessionToken={}",
			endpoint, hasText(sessionToken));
		return protectedPost(endpoint, new DropsPanelSettingRequest(enabled), sessionToken, PluginSettings.class);
	}

	@Override
	public CompletionStage<List<Team>> fetchStaffTeams(String sessionToken)
	{
		return protectedGet(url("plugin", "staff", "teams"), sessionToken, TEAM_LIST);
	}

	@Override
	public CompletionStage<Team> editStaffTeam(String teamId, TeamEditRequest request, String sessionToken)
	{
		return protectedPatch(url("plugin", "staff", "teams", teamId), request, sessionToken, Team.class);
	}

	@Override
	public CompletionStage<ActionResult> closeStaffTeam(String teamId, String sessionToken)
	{
		return protectedDelete(url("plugin", "staff", "teams", teamId), sessionToken, ActionResult.class);
	}

	@Override
	public CompletionStage<ActionResult> refreshEventCache(String sessionToken)
	{
		return unsupported("Event cache refresh requires a Railway API endpoint");
	}

	@Override
	public CompletionStage<ActionResult> closeTeam(String teamId, String sessionToken)
	{
		return protectedPost(url("plugin", "teams", teamId, "close"), new Object(), sessionToken, ActionResult.class);
	}

	@Override
	public CompletionStage<ActionResult> lockTeam(String teamId, String sessionToken)
	{
		return unsupported("Team locking requires a Railway API endpoint");
	}

	private <T> CompletionStage<T> get(HttpUrl url, String token, Type responseType)
	{
		Request.Builder request = new Request.Builder().url(url).get();
		addAuthorization(request, token);
		return execute(request.build(), responseType);
	}

	private <T> CompletionStage<T> protectedGet(HttpUrl url, String token, Type responseType)
	{
		logProtected("GET", url, token);
		return get(url, token, responseType);
	}

	private <T> CompletionStage<T> post(HttpUrl url, Object body, String token, Type responseType)
	{
		RequestBody requestBody = RequestBody.create(JSON, gson.toJson(body));
		Request.Builder request = new Request.Builder().url(url).post(requestBody);
		addAuthorization(request, token);
		return execute(request.build(), responseType);
	}

	private <T> CompletionStage<T> protectedPost(HttpUrl url, Object body, String token, Type responseType)
	{
		logProtected("POST", url, token);
		return post(url, body, token, responseType);
	}

	private <T> CompletionStage<T> patch(HttpUrl url, Object body, String token, Type responseType)
	{
		RequestBody requestBody = RequestBody.create(JSON, gson.toJson(body));
		Request.Builder request = new Request.Builder().url(url).patch(requestBody);
		addAuthorization(request, token);
		return execute(request.build(), responseType);
	}

	private <T> CompletionStage<T> protectedPatch(HttpUrl url, Object body, String token, Type responseType)
	{
		logProtected("PATCH", url, token);
		return patch(url, body, token, responseType);
	}

	private <T> CompletionStage<T> delete(HttpUrl url, String token, Type responseType)
	{
		Request.Builder request = new Request.Builder().url(url).delete();
		addAuthorization(request, token);
		return execute(request.build(), responseType);
	}

	private <T> CompletionStage<T> protectedDelete(HttpUrl url, String token, Type responseType)
	{
		logProtected("DELETE", url, token);
		return delete(url, token, responseType);
	}

	private static void logProtected(String method, HttpUrl url, String token)
	{
		log.info("Rancour API protected action: {} {} hasSessionToken={}", method, url, hasText(token));
	}

	private <T> CompletionStage<T> execute(Request request, Type responseType)
	{
		CompletableFuture<T> future = new CompletableFuture<>();
		if (configurationError != null)
		{
			log.error("Rancour API configuration error for {} {}: {}", request.method(), request.url(), configurationError.getMessage());
			future.completeExceptionally(configurationError);
			return future;
		}
		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException exception)
			{
				log.error("Rancour API network failure for {} {}", request.method(), request.url(), exception);
				future.completeExceptionally(new ApiException(
					"Cannot connect to the Rancour API. Check your connection and the Railway API service. "
						+ "Details: " + safeExceptionMessage(exception)
				));
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				try (ResponseBody body = response.body())
				{
					String json = body == null ? "" : body.string();
					if (!response.isSuccessful())
					{
						log.error("Rancour API non-success response: {} {} -> HTTP {} body={}",
							request.method(), request.url(), response.code(), redactForLog(json));
						ApiError error = json.isEmpty() ? null : gson.fromJson(json, ApiError.class);
						String message = error != null && hasText(error.message) ? error.message : "Rancour API returned HTTP " + response.code();
						future.completeExceptionally(new ApiException(message, response.code()));
						return;
					}
					log.info("Rancour API response: {} {} -> HTTP {} body={}",
						request.method(), request.url(), response.code(), safeResponseMessage(json));
					if (json.isEmpty())
					{
						log.error("Rancour API returned an empty response: {} {} -> HTTP {}", request.method(), request.url(), response.code());
						future.completeExceptionally(new ApiException("Rancour API returned an empty response", response.code()));
						return;
					}
					future.complete(gson.fromJson(json, responseType));
				}
				catch (Exception exception)
				{
					log.error("Rancour API response handling failed for {} {}", request.method(), request.url(), exception);
					future.completeExceptionally(new ApiException("Could not read the Rancour API response: " + exception.getMessage()));
				}
			}
		});
		return future;
	}

	private HttpUrl url(String... segments)
	{
		HttpUrl.Builder builder = baseUrl.newBuilder();
		for (String segment : segments)
		{
			builder.addPathSegment(segment);
		}
		return builder.build();
	}

	private static void addAuthorization(Request.Builder request, String token)
	{
		if (hasText(token))
		{
			request.header("Authorization", "Bearer " + token);
		}
	}

	private static String normalizeBaseUrl(String value)
	{
		String trimmed = value == null ? "" : value.trim();
		while (trimmed.endsWith("/"))
		{
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed + "/";
	}

	static String redactForLog(String value)
	{
		String redacted = JSON_SECRET.matcher(value == null ? "" : value).replaceAll("$1<redacted>$2");
		redacted = BEARER_SECRET.matcher(redacted).replaceAll("$1<redacted>");
		if (redacted.length() > MAX_LOGGED_BODY_LENGTH)
		{
			return redacted.substring(0, MAX_LOGGED_BODY_LENGTH) + "...<truncated>";
		}
		return redacted;
	}

	private static String safeResponseMessage(String value)
	{
		String redacted = redactForLog(value);
		if (redacted.length() <= 240)
		{
			return redacted;
		}
		return redacted.substring(0, 240) + "...<truncated>";
	}

	private static String safeExceptionMessage(Exception exception)
	{
		String message = exception.getMessage();
		return hasText(message) ? message : exception.getClass().getSimpleName();
	}

	private static boolean hasText(String value)
	{
		return value != null && !value.trim().isEmpty();
	}

	private static <T> CompletionStage<T> unsupported(String message)
	{
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(new UnsupportedOperationException(message));
		return future;
	}

	private static final class ApiError
	{
		private String message;
	}
}
