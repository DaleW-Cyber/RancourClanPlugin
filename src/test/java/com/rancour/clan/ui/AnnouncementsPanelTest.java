package com.rancour.clan.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.junit.Test;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.services.AnnouncementService;

public class AnnouncementsPanelTest
{
	@Test
	public void announcementCardsShowOnlyTitleAndBody() throws Exception
	{
		AnnouncementsPanel panel = new AnnouncementsPanel(() -> CompletableFuture.completedFuture(Collections.singletonList(
			new Announcement("id", "Title", "Body", "urgent", "2026-06-22T18:30:00Z", "2026-06-23T18:30:00Z", "Author", true)
		)));
		SwingUtilities.invokeAndWait(() -> { });

		String text = allText(panel);
		assertTrue(text.contains("Title"));
		assertTrue(text.contains("Body"));
		assertFalse(text.contains("Author"));
		assertFalse(text.contains("urgent"));
	}

	@Test
	public void refreshDoesNotOverlapRequests()
	{
		BlockingAnnouncements service = new BlockingAnnouncements();
		AnnouncementsPanel panel = new AnnouncementsPanel(service);
		panel.refresh();
		panel.refresh();
		assertEquals(1, service.calls.get());
		service.complete();
	}

	private static String allText(Container container)
	{
		StringBuilder text = new StringBuilder();
		for (Component component : container.getComponents())
		{
			if (component instanceof JTextArea)
			{
				text.append(((JTextArea) component).getText()).append('\n');
			}
			if (component instanceof Container)
			{
				text.append(allText((Container) component));
			}
		}
		return text.toString();
	}

	private static final class BlockingAnnouncements implements AnnouncementService
	{
		private final AtomicInteger calls = new AtomicInteger();
		private final CompletableFuture<java.util.List<Announcement>> future = new CompletableFuture<>();

		@Override
		public CompletionStage<java.util.List<Announcement>> loadAnnouncements()
		{
			calls.incrementAndGet();
			return future;
		}

		private void complete()
		{
			future.complete(Collections.emptyList());
		}
	}
}
