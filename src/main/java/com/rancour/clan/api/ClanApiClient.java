package com.rancour.clan.api;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.ApiHealth;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.DropSubmission;
import com.rancour.clan.models.DropSubmissionResult;
import com.rancour.clan.models.PluginSettings;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.StaffDropSubmission;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamEditRequest;
import com.rancour.clan.models.VerificationStartResponse;
import com.rancour.clan.models.VerificationStatus;

public interface ClanApiClient
{
	CompletionStage<ApiHealth> health();
	CompletionStage<VerificationStartResponse> startVerification();
	CompletionStage<VerificationStatus> fetchVerificationStatus(String verificationId, String sessionToken);
	CompletionStage<MemberProfile> fetchProfile(String sessionToken);
	CompletionStage<PluginSettings> fetchSettings();
	CompletionStage<List<Announcement>> fetchAnnouncements(String sessionToken);
	CompletionStage<List<ClanEvent>> fetchEvents(String sessionToken);
	CompletionStage<ActionResult> joinEvent(String eventId, String sessionToken);
	CompletionStage<ActionResult> leaveEvent(String eventId, String sessionToken);
	CompletionStage<DropSubmissionResult> submitDrop(DropSubmission submission, String sessionToken);
	CompletionStage<List<Team>> fetchTeams(String sessionToken);
	CompletionStage<ActionResult> joinTeam(String teamId, String sessionToken);
	CompletionStage<ActionResult> leaveTeam(String teamId, String sessionToken);
	CompletionStage<List<StaffDropSubmission>> fetchPendingDrops(String sessionToken);
	CompletionStage<ActionResult> approveDrop(String submissionId, String sessionToken);
	CompletionStage<ActionResult> rejectDrop(String submissionId, String sessionToken);
	CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request, String sessionToken);
	CompletionStage<ActionResult> deleteAnnouncement(String announcementId, String sessionToken);
	CompletionStage<PluginSettings> setDropsPanelEnabled(boolean enabled, String sessionToken);
	CompletionStage<List<Team>> fetchStaffTeams(String sessionToken);
	CompletionStage<Team> editStaffTeam(String teamId, TeamEditRequest request, String sessionToken);
	CompletionStage<ActionResult> closeStaffTeam(String teamId, String sessionToken);

	// TODO(Railway API): these actions need endpoints added to the agreed API contract.
	CompletionStage<ActionResult> refreshEventCache(String sessionToken);
	CompletionStage<ActionResult> closeTeam(String teamId, String sessionToken);
	CompletionStage<ActionResult> lockTeam(String teamId, String sessionToken);
}
