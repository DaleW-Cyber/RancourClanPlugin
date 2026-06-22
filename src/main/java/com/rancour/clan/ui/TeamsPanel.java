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
import com.rancour.clan.models.Team;
import com.rancour.clan.services.TeamService;

final class TeamsPanel extends JPanel
{
	private final TeamService service;
	private final JTextArea status = UiComponents.statusLabel("Not loaded");
	private final JPanel content = UiComponents.contentPanel();
	private boolean loading;

	TeamsPanel(TeamService service)
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
		status.setText("Loading teams...");
		service.loadTeams().whenComplete((items, error) -> SwingUtilities.invokeLater(() -> render(items, error)));
	}

	private void render(List<Team> items, Throwable error)
	{
		loading = false;
		content.removeAll();
		content.add(UiComponents.heading("Teams"));
		if (error != null)
		{
			status.setText("Error: " + UiComponents.errorMessage(error));
			content.add(UiComponents.wrapped("Teams are unavailable."));
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
				JPanel card = UiComponents.detailsCard(item.getActivity(), "",
					"Host", item.getHost(),
					"Members", item.getCurrentMembers() + "/" + item.getCapacity(),
					"Joined", item.getJoinedMembers().isEmpty() ? "None" : String.join(", ", item.getJoinedMembers()),
					"World", String.valueOf(item.getWorld()),
					"Voice", item.isVoiceRequired() ? "Required" : "Optional",
					"Status", item.getStatus());
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
		status.setText("Saving team signup...");
		action.whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			status.setText(error == null ? result.getMessage() : "Error: " + UiComponents.errorMessage(error));
			if (error == null) { refresh(); }
		}));
	}
}
