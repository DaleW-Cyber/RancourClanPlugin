package com.rancour.clan.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import org.junit.Test;
import com.rancour.clan.api.MockClanApiClient;

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
			assertEquals("Your verification session has expired. Refresh verification or link again.", error.getCause().getMessage());
			return;
		}
		throw new AssertionError("Expected event join to fail without a session token");
	}
}
