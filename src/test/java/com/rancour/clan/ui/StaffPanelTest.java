package com.rancour.clan.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.StaffDropSubmission;
import com.rancour.clan.services.StaffService;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.Test;

public class StaffPanelTest
{
	@Test
	public void createAnnouncementSubmitsTypedRequestAndRefreshesNews() throws Exception
	{
		RecordingStaffService service = new RecordingStaffService();
		AtomicInteger refreshes = new AtomicInteger();
		StaffPanel panel = new StaffPanel(service, refreshes::incrementAndGet);

		SwingUtilities.invokeAndWait(() ->
		{
			panel.announcementTitle.setText("Clan update");
			panel.announcementMessage.setText("Body for the news page");
			panel.createAnnouncementButton.doClick();
		});
		SwingUtilities.invokeAndWait(() -> { });

		CreateAnnouncementRequest request = service.created.get();
		assertNotNull(request);
		assertEquals("Clan update", request.getTitle());
		assertEquals("Body for the news page", request.getMessage());
		assertEquals("normal", request.getPriority());
		Instant expiresAt = Instant.parse(request.getExpiresAt());
		assertTrue(Duration.between(Instant.now(), expiresAt).toMinutes() <= 61);
		assertEquals(1, refreshes.get());

		SwingUtilities.invokeAndWait(() ->
		{
			assertEquals("", panel.announcementTitle.getText());
			assertEquals("", panel.announcementMessage.getText());
		});
	}

	@Test
	public void createAnnouncementValidatesTitleAndBodyBeforeSubmitting() throws Exception
	{
		RecordingStaffService service = new RecordingStaffService();
		StaffPanel panel = new StaffPanel(service);

		SwingUtilities.invokeAndWait(() -> panel.createAnnouncementButton.doClick());

		assertFalse(service.created.get() != null);
	}

	private static final class RecordingStaffService implements StaffService
	{
		private final AtomicReference<CreateAnnouncementRequest> created = new AtomicReference<>();

		@Override
		public CompletionStage<List<StaffDropSubmission>> loadPendingDrops()
		{
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		@Override
		public CompletionStage<ActionResult> approveDrop(String submissionId)
		{
			return CompletableFuture.completedFuture(new ActionResult(true, "approved"));
		}

		@Override
		public CompletionStage<ActionResult> rejectDrop(String submissionId)
		{
			return CompletableFuture.completedFuture(new ActionResult(true, "rejected"));
		}

		@Override
		public CompletionStage<Announcement> createAnnouncement(CreateAnnouncementRequest request)
		{
			created.set(request);
			return CompletableFuture.completedFuture(new Announcement("id", request.getTitle(), request.getMessage(),
				request.getPriority(), Instant.now().toString(), request.getExpiresAt(), "Staff", false));
		}

		@Override
		public CompletionStage<ActionResult> refreshEventCache()
		{
			return CompletableFuture.completedFuture(new ActionResult(true, "refreshed"));
		}
	}
}
