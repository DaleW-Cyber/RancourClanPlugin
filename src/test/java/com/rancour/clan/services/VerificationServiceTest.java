package com.rancour.clan.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}
