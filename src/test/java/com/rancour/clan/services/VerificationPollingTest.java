package com.rancour.clan.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import com.rancour.clan.api.ClanApiClient;
import com.rancour.clan.models.VerificationStatus;

public class VerificationPollingTest
{
	@Test
	public void unlinkedClientDoesNotCallApi() throws Exception
	{
		ClanApiClient api = (ClanApiClient) Proxy.newProxyInstance(
			ClanApiClient.class.getClassLoader(),
			new Class<?>[] {ClanApiClient.class},
			(proxy, method, args) ->
			{
				throw new AssertionError("Unexpected API call: " + method.getName());
			});
		VerificationService service = ApiServices.verification(api, new InMemorySessionStore());

		VerificationStatus status = service.refreshStatus().toCompletableFuture().get();

		assertEquals("unlinked", status.getState());
		assertFalse(status.isVerified());
		assertEquals("", service.getSessionToken());
	}

	@Test
	public void pendingVerificationStillPollsApi() throws Exception
	{
		AtomicInteger calls = new AtomicInteger();
		ClanApiClient api = (ClanApiClient) Proxy.newProxyInstance(
			ClanApiClient.class.getClassLoader(),
			new Class<?>[] {ClanApiClient.class},
			(proxy, method, args) ->
			{
				if ("fetchVerificationStatus".equals(method.getName()))
				{
					calls.incrementAndGet();
					return CompletableFuture.completedFuture(
						new VerificationStatus("pending", "", null, "later", "now")
					);
				}
				throw new AssertionError("Unexpected API call: " + method.getName());
			});
		InMemorySessionStore sessions = new InMemorySessionStore();
		sessions.setPendingVerificationId("pending-id");
		VerificationService service = ApiServices.verification(api, sessions);

		VerificationStatus status = service.refreshStatus().toCompletableFuture().get();

		assertEquals("pending", status.getState());
		assertEquals(1, calls.get());
	}
}
