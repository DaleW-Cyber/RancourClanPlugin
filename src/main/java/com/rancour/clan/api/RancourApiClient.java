package com.rancour.clan.api;

import com.rancour.clan.RancourClanConfig;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.models.MemberProfile;
import java.time.Instant;
import java.util.List;

public class RancourApiClient
{
    private final RancourClanConfig config;

    public RancourApiClient(RancourClanConfig config)
    {
        this.config = config;
    }

    public String getVerificationStatus()
    {
        MemberProfile profile = new MemberProfile(false, "Not linked", "Unknown", "Unknown");
        return "Verification\n\n"
            + "API: " + config.apiBaseUrl() + "\n"
            + "Status: " + profile.getStatus() + "\n"
            + "RSN: " + profile.getRsn() + "\n"
            + "Rank: " + profile.getRank() + "\n\n"
            + "Future behaviour:\n"
            + "1. Generate a link code in RuneLite.\n"
            + "2. User runs /plugin_link <code> in Discord.\n"
            + "3. API returns a verified plugin session.";
    }

    public String getAnnouncements()
    {
        List<Announcement> announcements = List.of(
            new Announcement("Welcome", "Rancour plugin announcement feed placeholder.", "normal", Instant.now())
        );

        StringBuilder builder = new StringBuilder("Announcements\n\n");
        for (Announcement announcement : announcements)
        {
            builder.append("[")
                .append(announcement.getPriority())
                .append("] ")
                .append(announcement.getTitle())
                .append("\n")
                .append(announcement.getMessage())
                .append("\n\n");
        }
        return builder.toString();
    }

    public String getEvents()
    {
        List<ClanEvent> events = List.of(
            new ClanEvent("Discord Event Placeholder", "Linked Discord events will appear here.", Instant.now())
        );

        StringBuilder builder = new StringBuilder("Events\n\n");
        for (ClanEvent event : events)
        {
            builder.append(event.getTitle())
                .append("\n")
                .append(event.getDescription())
                .append("\nStarts: ")
                .append(event.getStartTime())
                .append("\n\n");
        }
        return builder.toString();
    }

    public String submitDrop(String boss, String item)
    {
        return "Drop submission placeholder: " + boss + " - " + item;
    }
}
