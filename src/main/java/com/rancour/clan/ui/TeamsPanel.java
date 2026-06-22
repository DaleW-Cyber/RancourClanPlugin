package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Team;
import com.rancour.clan.services.TeamService;

final class TeamsPanel extends JPanel
{
	private final TeamService service;
	private final JLabel status = new JLabel("Not loaded");
	private final JPanel content = UiComponents.contentPanel();

	TeamsPanel(TeamService service)
	{
		super(new BorderLayout());
		this.service = service;
		JButton refresh = new JButton("Refresh Teams");
		refresh.addActionListener(event -> refresh());
		JPanel controls = new JPanel(new BorderLayout());
		controls.add(refresh, BorderLayout.CENTER);
		controls.add(status, BorderLayout.SOUTH);
		add(UiComponents.page(controls, content), BorderLayout.CENTER);
		refresh();
	}

	private void refresh()
	{
		status.setText("Loading teams...");
		service.loadTeams().whenComplete((items, error) -> SwingUtilities.invokeLater(() -> render(items, error)));
	}

	private void render(List<Team> items, Throwable error)
	{
		content.removeAll();
		content.add(UiComponents.heading("Team Finder"));
		if (error != null)
		{
			status.setText("Error: " + UiComponents.errorMessage(error));
			content.add(UiComponents.wrapped("Teams are unavailable. Try refresh when the API is online."));
		}
		else if (items == null || items.isEmpty())
		{
			status.setText("No open teams");
			content.add(UiComponents.wrapped("No open teams were returned."));
		}
		else
		{
			status.setText(items.size() + " team(s)");
			for (Team item : items)
			{
				String details = "Host: " + item.getHost() + " | Roles: " + String.join(", ", item.getRequiredRoles())
					+ " | Members: " + item.getCurrentMembers() + "/" + item.getCapacity() + " | World: " + item.getWorld()
					+ " | Voice: " + (item.isVoiceRequired() ? "Required" : "Optional");
				String tags = (item.isStaffHosted() ? "Staff-hosted | " : "") + String.join(", ", item.getTags()) + " | " + item.getStatus();
				JPanel card = UiComponents.card(item.getActivity(), details, tags);
				JPanel actions = new JPanel(new GridLayout(1, 2, 4, 0));
				JButton join = new JButton("Join");
				JButton leave = new JButton("Leave");
				join.addActionListener(event -> action(service.join(item.getId())));
				leave.addActionListener(event -> action(service.leave(item.getId())));
				actions.add(join);
				actions.add(leave);
				card.add(actions);
				content.add(card);
			}
		}
		content.revalidate();
		content.repaint();
	}

	private void action(CompletionStage<ActionResult> action)
	{
		status.setText("Saving team signup...");
		action.whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			status.setText(error == null ? result.getMessage() : "Error: " + UiComponents.errorMessage(error));
			if (error == null) { refresh(); }
		}));
	}
}
