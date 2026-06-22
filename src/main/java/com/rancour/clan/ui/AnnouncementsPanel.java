package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.services.AnnouncementService;

final class AnnouncementsPanel extends JPanel
{
	private final AnnouncementService service;
	private final JTextArea status = UiComponents.statusLabel("Not loaded");
	private final JPanel content = UiComponents.contentPanel();
	private boolean loading;

	AnnouncementsPanel(AnnouncementService service)
	{
		super(new BorderLayout());
		this.service = service;
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener(event -> refresh());
		JPanel controls = new JPanel(new BorderLayout());
		controls.add(refresh, BorderLayout.CENTER);
		controls.add(status, BorderLayout.SOUTH);
		add(UiComponents.page(controls, content), BorderLayout.CENTER);
		refresh();
	}

	void refresh()
	{
		if (loading)
		{
			return;
		}
		loading = true;
		status.setText("Loading announcements...");
		service.loadAnnouncements().whenComplete((items, error) -> SwingUtilities.invokeLater(() -> render(items, error)));
	}

	private void render(List<Announcement> items, Throwable error)
	{
		loading = false;
		content.removeAll();
		content.add(UiComponents.heading("Announcements"));
		if (error != null)
		{
			status.setText("Error: " + UiComponents.errorMessage(error));
			content.add(UiComponents.wrapped("Announcements are unavailable."));
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
				content.add(UiComponents.detailsCard(item.getTitle(), item.getMessage(), announcementAccent(item.getPriority())));
			}
			content.add(UiComponents.small("Last updated " + UiComponents.nowShort()));
		}
		content.revalidate();
		content.repaint();
	}

	private static Color announcementAccent(String priority)
	{
		if ("urgent".equalsIgnoreCase(priority))
		{
			return RancourTheme.BRAND_RED;
		}
		if ("high".equalsIgnoreCase(priority))
		{
			return RancourTheme.WARNING;
		}
		return RancourTheme.BRAND_RED_MUTED;
	}
}
