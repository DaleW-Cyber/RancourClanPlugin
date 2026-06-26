package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import com.rancour.clan.models.ClanEvent;
import com.rancour.clan.services.EventService;

final class EventsPanel extends JPanel
{
	private final EventService service;
	private final JTextArea status = UiComponents.statusLabel("Not loaded");
	private final JPanel content = UiComponents.contentPanel();
	private boolean loading;

	EventsPanel(EventService service)
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
		status.setText("Loading events...");
		service.loadEvents().whenComplete((items, error) -> SwingUtilities.invokeLater(() -> render(items, error)));
	}

	private void render(List<ClanEvent> items, Throwable error)
	{
		loading = false;
		content.removeAll();
		content.add(UiComponents.heading("Events"));
		if (error != null)
		{
			status.setText("Error: " + UiComponents.errorMessage(error));
			content.add(UiComponents.wrapped("Events are unavailable."));
		}
		else if (items == null || items.isEmpty())
		{
			status.setText("No upcoming events");
			content.add(UiComponents.wrapped("No events available for your Discord roles."));
		}
		else
		{
			status.setText(items.size() + " event(s)");
			for (ClanEvent item : items)
			{
				JPanel card = UiComponents.detailsCard(item.getName(), item.getDescription(), eventAccent(item),
					"Starts", UiComponents.shortDate(item.getStartTime()),
					"Countdown", countdown(item.getStartTime()));
				JTextArea badge = visibilityBadge(item);
				if (badge != null)
				{
					card.add(badge);
				}
				content.add(card);
			}
			content.add(UiComponents.small("Last updated " + UiComponents.nowShort()));
		}
		content.revalidate();
		content.repaint();
	}

	private static Color eventAccent(ClanEvent event)
	{
		if ("cancelled".equalsIgnoreCase(event.getStatus()) || "closed".equalsIgnoreCase(event.getStatus()))
		{
			return RancourTheme.DISABLED;
		}
		try
		{
			Instant start = Instant.parse(event.getStartTime());
			Duration until = Duration.between(Instant.now(), start);
			if (!until.isNegative() && until.toHours() <= 24)
			{
				return RancourTheme.WARNING;
			}
			if (until.isNegative())
			{
				return RancourTheme.DISABLED;
			}
		}
		catch (DateTimeParseException ignored)
		{
			return RancourTheme.INFO;
		}
		return RancourTheme.INFO;
	}

	private static JTextArea visibilityBadge(ClanEvent event)
	{
		String visibility = UiComponents.value(event.getVisibility()).trim();
		if ("staff".equalsIgnoreCase(visibility))
		{
			return UiComponents.badge("STAFF EVENT", RancourTheme.BRAND_RED);
		}
		if ("restricted".equalsIgnoreCase(visibility))
		{
			return UiComponents.badge("RESTRICTED", RancourTheme.WARNING);
		}
		return null;
	}

	private static String countdown(String startTime)
	{
		try
		{
			Duration duration = Duration.between(Instant.now(), Instant.parse(startTime));
			boolean past = duration.isNegative();
			Duration absolute = past ? duration.negated() : duration;
			long days = absolute.toDays();
			long hours = absolute.minusDays(days).toHours();
			long minutes = absolute.minusDays(days).minusHours(hours).toMinutes();
			String value;
			if (days > 0)
			{
				value = days + "d " + hours + "h";
			}
			else if (hours > 0)
			{
				value = hours + "h " + minutes + "m";
			}
			else
			{
				value = Math.max(0, minutes) + "m";
			}
			return past ? "Started " + value + " ago" : "Starts in " + value;
		}
		catch (DateTimeParseException ignored)
		{
			return "Unknown";
		}
	}
}
