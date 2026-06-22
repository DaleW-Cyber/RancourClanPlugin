package com.rancour.clan.ui;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.services.EventService;

final class EventsPanel extends JPanel
{
	EventsPanel(EventService service)
	{
		super(new BorderLayout());
		JPanel page = UiComponents.page();
		JPanel content = new JPanel();
		content.setOpaque(false);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(UiComponents.heading("Events"));
		for (ClanEvent event : service.getUpcomingEvents())
		{
			content.add(UiComponents.card(event.getName(), event.getDetails(), event.getSchedule()));
		}
		page.add(content, BorderLayout.NORTH);
		add(page, BorderLayout.CENTER);
	}
}
