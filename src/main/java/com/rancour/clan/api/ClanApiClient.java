package com.rancour.clan.api;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.models.DropSubmission;
import com.rancour.clan.models.VerificationStatus;

public interface ClanApiClient
{
	CompletionStage<VerificationStatus> fetchVerificationStatus();

	CompletionStage<List<Announcement>> fetchAnnouncements();

	CompletionStage<List<ClanEvent>> fetchEvents();

	CompletionStage<Void> submitDrop(DropSubmission submission);
}
