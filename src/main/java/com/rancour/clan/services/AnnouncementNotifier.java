package com.rancour.clan.services;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import com.rancour.clan.config.AnnouncementPriority;
import com.rancour.clan.models.Announcement;

public final class AnnouncementNotifier
{
	private static final int MAX_SEEN_IDS = 500;
	private static final int MAX_TITLE_LENGTH = 80;
	private final SeenAnnouncementStore store;
	private final Consumer<String> chatSink;

	public AnnouncementNotifier(SeenAnnouncementStore store, Consumer<String> chatSink)
	{
		this.store = store;
		this.chatSink = chatSink;
	}

	public synchronized void accept(List<Announcement> announcements, boolean enabled,
		AnnouncementPriority minimumPriority)
	{
		if (announcements == null)
		{
			return;
		}
		Set<String> seen = new LinkedHashSet<>(store.load());
		for (Announcement announcement : announcements)
		{
			String id = announcement.getId();
			if (id == null || id.trim().isEmpty() || !seen.add(id))
			{
				continue;
			}
			if (enabled && minimumPriority.includes(announcement.getPriority()))
			{
				chatSink.accept("[Rancour] New announcement: " + shortTitle(announcement.getTitle())
					+ ". Open the Rancour panel for details.");
			}
		}
		while (seen.size() > MAX_SEEN_IDS)
		{
			Iterator<String> iterator = seen.iterator();
			iterator.next();
			iterator.remove();
		}
		store.save(seen);
	}

	private static String shortTitle(String title)
	{
		String value = title == null || title.trim().isEmpty() ? "Clan update" : title.trim();
		return value.length() <= MAX_TITLE_LENGTH ? value : value.substring(0, MAX_TITLE_LENGTH - 3) + "...";
	}
}
