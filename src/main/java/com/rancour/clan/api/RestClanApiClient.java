package com.rancour.clan.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.models.DropSubmission;
import com.rancour.clan.models.VerificationStatus;

public final class RestClanApiClient implements ClanApiClient
{
	private final String baseUrl;

	public RestClanApiClient(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}

	@Override
	public CompletionStage<VerificationStatus> fetchVerificationStatus()
	{
		return unavailable();
	}

	@Override
	public CompletionStage<List<Announcement>> fetchAnnouncements()
	{
		return unavailable();
	}

	@Override
	public CompletionStage<List<ClanEvent>> fetchEvents()
	{
		return unavailable();
	}

	@Override
	public CompletionStage<Void> submitDrop(DropSubmission submission)
	{
		return unavailable();
	}

	private static <T> CompletionStage<T> unavailable()
	{
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(new UnsupportedOperationException("REST API integration is planned for Phase 2"));
		return future;
	}
}
