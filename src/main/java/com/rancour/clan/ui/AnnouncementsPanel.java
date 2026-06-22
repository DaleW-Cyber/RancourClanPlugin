package com.rancour.clan.ui;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.services.AnnouncementService;

final class AnnouncementsPanel extends JPanel
{
	AnnouncementsPanel(AnnouncementService service)
	{
		super(new BorderLayout());
		JPanel page = UiComponents.page();
		JPanel content = new JPanel();
		content.setOpaque(false);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(UiComponents.heading("Announcements"));
		for (Announcement announcement : service.getAnnouncements())
		{
			content.add(UiComponents.card(announcement.getTitle(), announcement.getBody(), announcement.getPublishedAt()));
		}
		page.add(content, BorderLayout.NORTH);
		add(page, BorderLayout.CENTER);
	}
}
