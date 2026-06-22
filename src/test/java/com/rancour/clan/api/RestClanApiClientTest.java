package com.rancour.clan.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Test;

public class RestClanApiClientTest
{
	private static final MediaType JSON = MediaType.parse("application/json");

	@Test
	public void invalidBaseUrlFailsAsynchronouslyWithUsefulMessage() throws Exception
	{
		RestClanApiClient api = new RestClanApiClient(new OkHttpClient(), new Gson(), "not a URL");
		ApiException error = failure(api.fetchAnnouncements("").toCompletableFuture());
		assertTrue(error.getMessage().contains("API base URL is invalid"));
	}

	@Test
	public void trimsBaseUrlAndAvoidsDuplicateSlashes() throws Exception
	{
		AtomicReference<Request> captured = new AtomicReference<>();
		OkHttpClient client = responseClient(captured, 200,
			"{\"code\":\"ABC12345\",\"verificationId\":\"attempt\",\"expiresAt\":\"later\"}");
		RestClanApiClient api = new RestClanApiClient(client, new Gson(), "  https://api.example.test///  ");

		api.startVerification().toCompletableFuture().get();

		assertEquals("https://api.example.test/plugin/verification/start", captured.get().url().toString());
	}

	@Test
	public void healthUsesNormalizedBaseUrl() throws Exception
	{
		AtomicReference<Request> captured = new AtomicReference<>();
		RestClanApiClient api = new RestClanApiClient(
			responseClient(captured, 200, "{\"status\":\"ok\"}"),
			new Gson(),
			"https://api.example.test/"
		);

		assertEquals("ok", api.health().toCompletableFuture().get().getStatus());
		assertEquals("https://api.example.test/health", captured.get().url().toString());
	}

	@Test
	public void protectedEventJoinSendsBearerAuthorization() throws Exception
	{
		AtomicReference<Request> captured = new AtomicReference<>();
		RestClanApiClient api = new RestClanApiClient(
			responseClient(captured, 200, "{\"success\":true,\"message\":\"joined\"}"),
			new Gson(),
			"https://api.example.test"
		);

		api.joinEvent("event-1", "session-value").toCompletableFuture().get();

		assertEquals("POST", captured.get().method());
		assertEquals("https://api.example.test/plugin/events/event-1/join", captured.get().url().toString());
		assertEquals("Bearer session-value", captured.get().header("Authorization"));
	}

	@Test
	public void protectedTeamJoinSendsBearerAuthorization() throws Exception
	{
		AtomicReference<Request> captured = new AtomicReference<>();
		RestClanApiClient api = new RestClanApiClient(
			responseClient(captured, 200, "{\"success\":true,\"message\":\"joined\"}"),
			new Gson(),
			"https://api.example.test"
		);

		api.joinTeam("team-1", "team-session").toCompletableFuture().get();

		assertEquals("POST", captured.get().method());
		assertEquals("https://api.example.test/plugin/teams/team-1/join", captured.get().url().toString());
		assertEquals("Bearer team-session", captured.get().header("Authorization"));
	}

	@Test
	public void protectedStaffCreateAnnouncementSendsBearerAuthorization() throws Exception
	{
		AtomicReference<Request> captured = new AtomicReference<>();
		RestClanApiClient api = new RestClanApiClient(
			responseClient(captured, 200,
				"{\"id\":\"id\",\"title\":\"Title\",\"message\":\"Body\",\"priority\":\"normal\",\"createdAt\":\"now\",\"expiresAt\":\"later\",\"author\":\"Staff\",\"restricted\":false}"),
			new Gson(),
			"https://api.example.test"
		);

		api.createAnnouncement(new com.rancour.clan.models.CreateAnnouncementRequest("Title", "Body", "normal", "later"), "staff-session")
			.toCompletableFuture().get();

		assertEquals("POST", captured.get().method());
		assertEquals("https://api.example.test/plugin/staff/announcements", captured.get().url().toString());
		assertEquals("Bearer staff-session", captured.get().header("Authorization"));
	}

	@Test
	public void settingsUsePublicEndpoint() throws Exception
	{
		AtomicReference<Request> captured = new AtomicReference<>();
		RestClanApiClient api = new RestClanApiClient(
			responseClient(captured, 200, "{\"dropsPanelEnabled\":false,\"approvedDrops\":[\"Twisted bow\"]}"),
			new Gson(),
			"https://api.example.test"
		);

		assertFalse(api.fetchSettings().toCompletableFuture().get().isDropsPanelEnabled());
		assertEquals("GET", captured.get().method());
		assertEquals("https://api.example.test/plugin/settings", captured.get().url().toString());
	}

	@Test
	public void staffDeleteAnnouncementSendsBearerAuthorization() throws Exception
	{
		AtomicReference<Request> captured = new AtomicReference<>();
		RestClanApiClient api = new RestClanApiClient(
			responseClient(captured, 200, "{\"success\":true,\"message\":\"deleted\"}"),
			new Gson(),
			"https://api.example.test"
		);

		api.deleteAnnouncement("announcement-1", "staff-session").toCompletableFuture().get();

		assertEquals("DELETE", captured.get().method());
		assertEquals("https://api.example.test/plugin/staff/announcements/announcement-1", captured.get().url().toString());
		assertEquals("Bearer staff-session", captured.get().header("Authorization"));
	}

	@Test
	public void staffDropsPanelToggleSendsBearerAuthorization() throws Exception
	{
		AtomicReference<Request> captured = new AtomicReference<>();
		RestClanApiClient api = new RestClanApiClient(
			responseClient(captured, 200, "{\"dropsPanelEnabled\":true,\"approvedDrops\":[]}"),
			new Gson(),
			"https://api.example.test"
		);

		api.setDropsPanelEnabled(true, "staff-session").toCompletableFuture().get();

		assertEquals("POST", captured.get().method());
		assertEquals("https://api.example.test/plugin/staff/settings/drops-panel", captured.get().url().toString());
		assertEquals("Bearer staff-session", captured.get().header("Authorization"));
	}

	@Test
	public void nonSuccessIncludesStatusAndSafeApiMessage() throws Exception
	{
		RestClanApiClient api = new RestClanApiClient(
			responseClient(new AtomicReference<>(), 503, "{\"message\":\"Service warming up\"}"),
			new Gson(),
			"https://api.example.test"
		);

		ApiException error = failure(api.health().toCompletableFuture());
		assertEquals(503, error.getStatusCode());
		assertEquals("Service warming up", error.getMessage());
	}

	@Test
	public void networkFailureHasActionableUserMessage() throws Exception
	{
		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain ->
		{
			throw new IOException("DNS lookup failed");
		}).build();
		RestClanApiClient api = new RestClanApiClient(client, new Gson(), "https://api.example.test");

		ApiException error = failure(api.health().toCompletableFuture());
		assertTrue(error.getMessage().contains("Check the API base URL, HTTPS address, and Railway service"));
		assertTrue(error.getMessage().contains("DNS lookup failed"));
	}

	@Test
	public void loggedBodiesRedactTokensAndBearerSecrets()
	{
		String secret = "super-secret-session-value";
		String logged = RestClanApiClient.redactForLog(
			"{\"sessionToken\":\"" + secret + "\",\"message\":\"Bearer " + secret + "\"}"
		);
		assertFalse(logged.contains(secret));
		assertTrue(logged.contains("<redacted>"));
	}

	private static OkHttpClient responseClient(AtomicReference<Request> captured, int status, String body)
	{
		return new OkHttpClient.Builder().addInterceptor(chain ->
		{
			Request request = chain.request();
			captured.set(request);
			return new Response.Builder()
				.request(request)
				.protocol(Protocol.HTTP_1_1)
				.code(status)
				.message("test")
				.body(ResponseBody.create(JSON, body))
				.build();
		}).build();
	}

	private static ApiException failure(java.util.concurrent.CompletableFuture<?> future) throws Exception
	{
		try
		{
			future.get();
		}
		catch (ExecutionException error)
		{
			assertTrue(error.getCause() instanceof ApiException);
			return (ApiException) error.getCause();
		}
		throw new AssertionError("Expected API failure");
	}
}
