package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamCreateRequest;
import com.rancour.clan.models.TeamEditRequest;
import com.rancour.clan.services.TeamService;

final class TeamsPanel extends JPanel
{
	private static final String[][] CONTENT_OPTIONS = {
		{"cox", "Chambers of Xeric"},
		{"tob", "Theatre of Blood"},
		{"toa", "Tombs of Amascut"},
		{"nex", "Nex"},
		{"corp", "Corporeal Beast"},
		{"nightmare", "Nightmare"},
		{"other", "Other"}
	};
	private final TeamService service;
	private final Supplier<String> activeRsn;
	private final JTextArea status = UiComponents.statusLabel("Not loaded");
	private final JPanel content = UiComponents.contentPanel();
	private boolean loading;

	TeamsPanel(TeamService service)
	{
		this(service, () -> "");
	}

	TeamsPanel(TeamService service, Supplier<String> activeRsn)
	{
		super(new BorderLayout());
		this.service = service;
		this.activeRsn = activeRsn;
		JButton refresh = UiComponents.neutralButton("Refresh");
		JButton create = UiComponents.successButton("Create Team");
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
		JPanel card = UiComponents.card("Team", "", "", RancourTheme.INFO);
		JComboBox<String> contentType = UiComponents.compact(new JComboBox<>(contentLabels()));
		JTextField activity = UiComponents.compact(new JTextField());
		activity.setEnabled(false);
		JTextField capacity = UiComponents.compact(new JTextField("5"));
		JTextField world = UiComponents.compact(new JTextField("416"));
		JCheckBox voice = UiComponents.compact(new JCheckBox("Voice required"));
		JTextArea notes = new JTextArea(3, 12);
		notes.setLineWrap(true);
		notes.setWrapStyleWord(true);
		notes.setAlignmentX(Component.LEFT_ALIGNMENT);
		JButton create = UiComponents.successButton("Create");
		JButton back = UiComponents.neutralButton("Back");
		card.add(UiComponents.fieldRow("Content", ""));
		card.add(contentType);
		card.add(UiComponents.fieldRow("Other activity", ""));
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
		contentType.addActionListener(event ->
		{
			boolean other = "other".equals(selectedContentKey(contentType));
			activity.setEnabled(other);
			if (!other)
			{
				activity.setText("");
			}
		});
		create.addActionListener(event -> createTeam(contentType, activity, capacity, world, voice, notes, create));
		back.addActionListener(event -> refresh());
		status.setText("Enter team details");
		content.revalidate();
		content.repaint();
	}

	private void createTeam(JComboBox<String> contentType, JTextField activity, JTextField capacity, JTextField world, JCheckBox voice,
		JTextArea notes, JButton button)
	{
		String contentKey = selectedContentKey(contentType);
		String activityValue = "other".equals(contentKey) ? activity.getText().trim() : selectedContentName(contentType);
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
		if (capacityValue < 2 || capacityValue > 10)
		{
			status.setText("Capacity must be between 2 and 10");
			return;
		}
		if (worldValue < 300 || worldValue > 700)
		{
			status.setText("World must be between 300 and 700");
			return;
		}
		String notesValue = notes.getText().trim();
		if (notesValue.length() > 500)
		{
			status.setText("Notes must be 500 characters or fewer");
			return;
		}
		button.setEnabled(false);
		status.setText("Creating team...");
		TeamCreateRequest request = new TeamCreateRequest(activityValue, capacityValue, worldValue,
			voice.isSelected(), notesValue, null, "other".equals(contentKey) ? Collections.emptyList() : Collections.singletonList(contentKey));
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
				JPanel card = UiComponents.detailsCard(item.getActivity(), "", teamAccent(item),
					"Host", item.getHost(),
					"Members", item.getCurrentMembers() + "/" + item.getCapacity(),
					"Joined", item.getJoinedMembers().isEmpty() ? "None" : String.join(", ", item.getJoinedMembers()),
					"World", String.valueOf(item.getWorld()),
					"Voice", item.isVoiceRequired() ? "Required" : "Optional");
				card.add(UiComponents.badge(teamBadge(item), teamAccent(item)));
				if (item.isStaffHosted())
				{
					card.add(UiComponents.badge("STAFF HOSTED", RancourTheme.BRAND_RED));
				}
				JPanel actions = new JPanel(new GridLayout(2, 1, 0, 4));
				JButton join = UiComponents.successButton("Join");
				JButton leave = UiComponents.dangerButton("Leave");
				join.addActionListener(event -> action(service.join(item.getId())));
				leave.addActionListener(event -> action(service.leave(item.getId())));
				actions.add(join);
				actions.add(leave);
				card.add(actions);
				if (canManage(item))
				{
					JButton edit = UiComponents.neutralButton("Edit");
					JButton close = UiComponents.dangerButton("Close");
					edit.addActionListener(event -> showEditForm(item));
					close.addActionListener(event -> action(service.close(item.getId())));
					card.add(edit);
					card.add(close);
				}
				content.add(card);
			}
			content.add(UiComponents.small("Last updated " + UiComponents.nowShort()));
		}
		content.revalidate();
		content.repaint();
	}

	private boolean canManage(Team team)
	{
		String active = UiComponents.value(activeRsn.get()).trim();
		return !active.isEmpty() && active.equalsIgnoreCase(UiComponents.value(team.getHost()).trim());
	}

	private void showEditForm(Team team)
	{
		content.removeAll();
		content.add(UiComponents.heading("Edit Team"));
		JPanel card = UiComponents.card("Team", "", "", RancourTheme.INFO);
		JTextField activity = UiComponents.compact(new JTextField(team.getActivity()));
		JTextField capacity = UiComponents.compact(new JTextField(String.valueOf(team.getCapacity())));
		JTextField world = UiComponents.compact(new JTextField(String.valueOf(team.getWorld())));
		JCheckBox voice = UiComponents.compact(new JCheckBox("Voice required"));
		voice.setSelected(team.isVoiceRequired());
		JTextArea notes = new JTextArea(team.getNotes(), 3, 12);
		notes.setLineWrap(true);
		notes.setWrapStyleWord(true);
		JButton save = UiComponents.successButton("Save");
		JButton back = UiComponents.neutralButton("Back");
		card.add(UiComponents.fieldRow("Activity", ""));
		card.add(activity);
		card.add(UiComponents.fieldRow("Capacity", ""));
		card.add(capacity);
		card.add(UiComponents.fieldRow("World", ""));
		card.add(world);
		card.add(voice);
		card.add(UiComponents.fieldRow("Notes", ""));
		card.add(UiComponents.compact(notes));
		card.add(save);
		card.add(back);
		content.add(card);
		save.addActionListener(event -> editTeam(team, activity, capacity, world, voice, notes, save));
		back.addActionListener(event -> refresh());
		status.setText("Edit team details");
		content.revalidate();
		content.repaint();
	}

	private void editTeam(Team team, JTextField activity, JTextField capacity, JTextField world, JCheckBox voice,
		JTextArea notes, JButton button)
	{
		String activityValue = activity.getText().trim();
		Integer capacityValue = parseInt(capacity.getText());
		Integer worldValue = parseInt(world.getText());
		if (activityValue.isEmpty())
		{
			status.setText("Activity is required");
			return;
		}
		if (capacityValue == null || worldValue == null)
		{
			status.setText("Capacity and world must be numbers");
			return;
		}
		if (capacityValue < team.getCurrentMembers())
		{
			status.setText("Capacity cannot be below current members");
			return;
		}
		if (worldValue < 300 || worldValue > 700)
		{
			status.setText("World must be between 300 and 700");
			return;
		}
		String notesValue = notes.getText().trim();
		if (notesValue.length() > 500)
		{
			status.setText("Notes must be 500 characters or fewer");
			return;
		}
		button.setEnabled(false);
		status.setText("Saving team...");
		TeamEditRequest request = new TeamEditRequest(activityValue, capacityValue, worldValue,
			voice.isSelected(), notesValue, team.getTags(), team.getStatus());
		service.edit(team.getId(), request).whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			button.setEnabled(true);
			status.setText(error == null ? "Team updated" : "Error: " + UiComponents.errorMessage(error));
			if (error == null) { refresh(); }
		}));
	}

	private static Color teamAccent(Team team)
	{
		String status = UiComponents.value(team.getStatus());
		if ("closed".equalsIgnoreCase(status) || "expired".equalsIgnoreCase(status) || "filled".equalsIgnoreCase(status))
		{
			return RancourTheme.DISABLED;
		}
		if (team.getCurrentMembers() >= team.getCapacity())
		{
			return RancourTheme.DISABLED;
		}
		if (team.getCapacity() > 0 && team.getCurrentMembers() >= Math.max(1, team.getCapacity() - 1))
		{
			return RancourTheme.WARNING;
		}
		return RancourTheme.SUCCESS;
	}

	private static String teamBadge(Team team)
	{
		String status = UiComponents.value(team.getStatus());
		if ("closed".equalsIgnoreCase(status) || "expired".equalsIgnoreCase(status) || "filled".equalsIgnoreCase(status))
		{
			return status.toUpperCase();
		}
		if (team.getCurrentMembers() >= team.getCapacity())
		{
			return "FULL";
		}
		if (team.getCapacity() > 0 && team.getCurrentMembers() >= Math.max(1, team.getCapacity() - 1))
		{
			return "NEARLY FULL";
		}
		return "OPEN";
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

	private static String[] contentLabels()
	{
		return Arrays.stream(CONTENT_OPTIONS).map(item -> item[1]).toArray(String[]::new);
	}

	private static String selectedContentKey(JComboBox<String> contentType)
	{
		int index = Math.max(0, contentType.getSelectedIndex());
		return CONTENT_OPTIONS[Math.min(index, CONTENT_OPTIONS.length - 1)][0];
	}

	private static String selectedContentName(JComboBox<String> contentType)
	{
		int index = Math.max(0, contentType.getSelectedIndex());
		return CONTENT_OPTIONS[Math.min(index, CONTENT_OPTIONS.length - 1)][1];
	}
}
