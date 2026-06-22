package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import com.rancour.clan.models.ActionResult;
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
				JPanel card = UiComponents.detailsCard(item.getName(), item.getDescription(),
					"Starts", UiComponents.shortDate(item.getStartTime()),
					"Host", item.getHost(),
					"Status", item.getStatus(),
					"Signups", String.valueOf(item.getSignupCount()));
				JPanel actions = new JPanel(new GridLayout(2, 1, 0, 4));
				JButton join = new JButton("Join");
				JButton leave = new JButton("Leave");
				join.addActionListener(event -> action(service.join(item.getId())));
				leave.addActionListener(event -> action(service.leave(item.getId())));
				actions.add(join);
				actions.add(leave);
				card.add(actions);
				content.add(card);
			}
			content.add(UiComponents.small("Last updated " + UiComponents.nowShort()));
		}
		content.revalidate();
		content.repaint();
	}

	private void action(CompletionStage<ActionResult> action)
	{
		status.setText("Saving event signup...");
		action.whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null && UiComponents.isApiStatus(error, 403))
			{
				status.setText("You do not have access to this event.");
			}
			else
			{
				status.setText(error == null ? result.getMessage() : "Error: " + UiComponents.errorMessage(error));
			}
			if (error == null)
			{
				refresh();
			}
		}));
	}
}
