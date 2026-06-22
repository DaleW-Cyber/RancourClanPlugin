package com.rancour.clan.services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.Announcement;

public interface AnnouncementService
{
	CompletionStage<List<Announcement>> loadAnnouncements();
}
