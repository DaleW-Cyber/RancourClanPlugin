package com.rancour.clan.services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.EditAnnouncementRequest;
import com.rancour.clan.models.StaffDropSubmission;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamEditRequest;

public interface StaffService
{
	CompletionStage<List<Announcement>> loadAnnouncements();
	CompletionStage<List<StaffDropSubmission>> loadPendingDrops();
	CompletionStage<ActionResult> approveDrop(String submissionId);
	CompletionStage<ActionResult> rejectDrop(String submissionId);
	CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request);
	CompletionStage<Announcement> editAnnouncement(String announcementId, EditAnnouncementRequest request);
	CompletionStage<ActionResult> deleteAnnouncement(String announcementId);
	CompletionStage<com.rancour.clan.models.PluginSettings> setDropsPanelEnabled(boolean enabled);
	CompletionStage<com.rancour.clan.models.PluginSettings> setDropsAccessMode(String mode);
	CompletionStage<List<Team>> loadTeams();
	CompletionStage<Team> editTeam(String teamId, TeamEditRequest request);
	CompletionStage<ActionResult> closeTeam(String teamId);
	CompletionStage<ActionResult> refreshEventCache();
}
