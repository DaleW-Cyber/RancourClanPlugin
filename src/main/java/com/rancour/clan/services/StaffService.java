package com.rancour.clan.services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.StaffDropSubmission;

public interface StaffService
{
	CompletionStage<List<StaffDropSubmission>> loadPendingDrops();
	CompletionStage<ActionResult> approveDrop(String submissionId);
	CompletionStage<ActionResult> rejectDrop(String submissionId);
	CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request);
	CompletionStage<ActionResult> refreshEventCache();
}
