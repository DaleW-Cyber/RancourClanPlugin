package com.rancour.clan.api;

import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import java.util.concurrent.ExecutionException;
import okhttp3.OkHttpClient;
import org.junit.Test;

public class RestClanApiClientTest
{
	@Test
	public void invalidBaseUrlFailsAsynchronouslyWithUsefulMessage() throws Exception
	{
		RestClanApiClient api = new RestClanApiClient(new OkHttpClient(), new Gson(), "not a URL");
		try
		{
			api.fetchAnnouncements("").toCompletableFuture().get();
		}
		catch (ExecutionException error)
		{
			assertTrue(error.getCause() instanceof ApiException);
			assertTrue(error.getCause().getMessage().contains("API base URL is invalid"));
			return;
		}
		throw new AssertionError("Expected invalid URL failure");
	}
}
