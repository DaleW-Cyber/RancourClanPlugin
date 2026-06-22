package com.rancour.clan.services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.rancour.clan.config.RancourClanConfig;
import com.rancour.clan.models.Announcement;

public final class NotifyingAnnouncementService implements AnnouncementService
{
	private final AnnouncementService delegate;
	private final AnnouncementNotifier notifier;
	private final RancourClanConfig config;

	public NotifyingAnnouncementService(AnnouncementService delegate, AnnouncementNotifier notifier,
		RancourClanConfig config)
	{
		this.delegate = delegate;
		this.notifier = notifier;
		this.config = config;
	}

	@Override
	public CompletionStage<List<Announcement>> loadAnnouncements()
	{
		return delegate.loadAnnouncements().thenApply(items ->
		{
			notifier.accept(items, config.announcementChatNotifications(), config.announcementMinimumPriority());
			return items;
		});
	}
}
