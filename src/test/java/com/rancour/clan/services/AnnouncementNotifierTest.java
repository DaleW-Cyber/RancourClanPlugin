package com.rancour.clan.services;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import com.rancour.clan.config.AnnouncementPriority;
import com.rancour.clan.models.Announcement;

public class AnnouncementNotifierTest
{
	@Test
	public void doesNotNotifyForTheSameAnnouncementTwice()
	{
		MemoryStore store = new MemoryStore();
		List<String> messages = new ArrayList<>();
		AnnouncementNotifier notifier = new AnnouncementNotifier(store, messages::add);
		List<Announcement> announcements = Arrays.asList(
			new Announcement("one", "First update", "Body", "normal", "now", null, "Staff", false),
			new Announcement("two", "Urgent update", "Body", "urgent", "now", null, "Staff", false)
		);

		notifier.accept(announcements, true, AnnouncementPriority.NORMAL);
		notifier.accept(announcements, true, AnnouncementPriority.NORMAL);

		assertEquals(2, messages.size());
		assertEquals("[Rancour] <col=cc4040>ANNOUNCEMENT:</col> First update", messages.get(0));
	}

	@Test
	public void marksFilteredPrioritiesSeenWithoutNotifyingLater()
	{
		MemoryStore store = new MemoryStore();
		List<String> messages = new ArrayList<>();
		AnnouncementNotifier notifier = new AnnouncementNotifier(store, messages::add);
		List<Announcement> announcements = Arrays.asList(
			new Announcement("one", "Normal", "Body", "normal", "now", null, "Staff", false)
		);

		notifier.accept(announcements, true, AnnouncementPriority.HIGH);
		notifier.accept(announcements, true, AnnouncementPriority.NORMAL);

		assertEquals(0, messages.size());
	}

	private static final class MemoryStore implements SeenAnnouncementStore
	{
		private Set<String> values = new LinkedHashSet<>();

		@Override public Set<String> load() { return new LinkedHashSet<>(values); }
		@Override public void save(Set<String> announcementIds) { values = new LinkedHashSet<>(announcementIds); }
	}
}
