package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamCreateRequest;
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
		JButton create = new JButton("Create Team");
		refresh.addActionListener(event -> refresh());
		create.addActionListener(event -> showCreateForm());
		JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 4));
		buttons.add(refresh);
		buttons.add(create);
		JPanel controls = new JPanel(new BorderLayout(0, 4));
		controls.add(buttons, BorderLayout.CENTER);
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

	private void showCreateForm()
	{
		content.removeAll();
		content.add(UiComponents.heading("Create Team"));
		JPanel card = UiComponents.card("Team", "", "");
		JTextField activity = UiComponents.compact(new JTextField());
		JTextField capacity = UiComponents.compact(new JTextField("5"));
		JTextField world = UiComponents.compact(new JTextField("416"));
		JCheckBox voice = UiComponents.compact(new JCheckBox("Voice required"));
		JTextArea notes = new JTextArea(3, 12);
		notes.setLineWrap(true);
		notes.setWrapStyleWord(true);
		notes.setAlignmentX(Component.LEFT_ALIGNMENT);
		JButton create = UiComponents.compact(new JButton("Create"));
		JButton back = UiComponents.compact(new JButton("Back"));
		card.add(UiComponents.fieldRow("Activity", ""));
		card.add(activity);
		card.add(UiComponents.fieldRow("Capacity", ""));
		card.add(capacity);
		card.add(UiComponents.fieldRow("World", ""));
		card.add(world);
		card.add(voice);
		card.add(UiComponents.fieldRow("Notes", ""));
		card.add(UiComponents.compact(notes));
		card.add(create);
		card.add(back);
		content.add(card);
		create.addActionListener(event -> createTeam(activity, capacity, world, voice, notes, create));
		back.addActionListener(event -> refresh());
		status.setText("Enter team details");
		content.revalidate();
		content.repaint();
	}

	private void createTeam(JTextField activity, JTextField capacity, JTextField world, JCheckBox voice,
		JTextArea notes, JButton button)
	{
		String activityValue = activity.getText().trim();
		if (activityValue.isEmpty())
		{
			status.setText("Activity is required");
			return;
		}
		Integer capacityValue = parseInt(capacity.getText());
		Integer worldValue = parseInt(world.getText());
		if (capacityValue == null || worldValue == null)
		{
			status.setText("Capacity and world must be numbers");
			return;
		}
		button.setEnabled(false);
		status.setText("Creating team...");
		TeamCreateRequest request = new TeamCreateRequest(activityValue, capacityValue, worldValue,
			voice.isSelected(), notes.getText().trim(), "");
		service.create(request).whenComplete((team, error) -> SwingUtilities.invokeLater(() ->
		{
			button.setEnabled(true);
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				return;
			}
			status.setText("Team created");
			refresh();
		}));
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
					"Voice", item.isVoiceRequired() ? "Required" : "Optional");
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

	private static Integer parseInt(String value)
	{
		try
		{
			return Integer.parseInt(value.trim());
		}
		catch (NumberFormatException ignored)
		{
			return null;
		}
	}
}
