package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.services.AnnouncementService;

final class AnnouncementsPanel extends JPanel
{
	private final AnnouncementService service;
	private final JLabel status = new JLabel("Not loaded");
	private final JPanel content = UiComponents.contentPanel();

	AnnouncementsPanel(AnnouncementService service)
	{
		super(new BorderLayout());
		this.service = service;
		JButton refresh = new JButton("Refresh Announcements");
		refresh.addActionListener(event -> refresh());
		JPanel controls = new JPanel(new BorderLayout());
		controls.add(refresh, BorderLayout.CENTER);
		controls.add(status, BorderLayout.SOUTH);
		add(UiComponents.page(controls, content), BorderLayout.CENTER);
		refresh();
	}

	private void refresh()
	{
		status.setText("Loading announcements...");
		service.loadAnnouncements().whenComplete((items, error) -> SwingUtilities.invokeLater(() -> render(items, error)));
	}

	private void render(List<Announcement> items, Throwable error)
	{
		content.removeAll();
		content.add(UiComponents.heading("Announcements"));
		if (error != null)
		{
			status.setText("Error: " + UiComponents.errorMessage(error));
			content.add(UiComponents.wrapped("Announcements are unavailable. The rest of the plugin remains usable."));
		}
		else if (items == null || items.isEmpty())
		{
			status.setText("No announcements");
			content.add(UiComponents.wrapped("There are no active announcements."));
		}
		else
		{
			status.setText(items.size() + " announcement(s)");
			for (Announcement item : items)
			{
				String footer = "Priority: " + item.getPriority() + " | By: " + item.getAuthor()
					+ " | Created: " + item.getCreatedAt() + " | Expires: " + item.getExpiresAt();
				content.add(UiComponents.card(item.getTitle(), item.getMessage(), footer));
			}
		}
		content.revalidate();
		content.repaint();
	}
}
