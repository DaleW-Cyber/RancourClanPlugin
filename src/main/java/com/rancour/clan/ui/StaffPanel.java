package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.PluginSettings;
import com.rancour.clan.models.Team;
import com.rancour.clan.models.TeamEditRequest;
import com.rancour.clan.services.StaffService;

final class StaffPanel extends JPanel
{
	private final StaffService service;
	private final Runnable dataChanged;
	private final Predicate<String> confirmer;
	private final JTextArea status = UiComponents.statusLabel("Ready");
	private final JPanel content = UiComponents.contentPanel();
	private boolean dropsPanelEnabled = true;
	JButton announcementsButton;
	JButton dropsPanelButton;
	JButton teamsButton;
	JTextField announcementTitle;
	JTextArea announcementMessage;
	JButton createAnnouncementButton;

	StaffPanel(StaffService service)
	{
		this(service, () -> { });
	}

	StaffPanel(StaffService service, Runnable dataChanged)
	{
		this(service, dataChanged, null);
	}

	StaffPanel(StaffService service, Runnable dataChanged, Predicate<String> confirmer)
	{
		super(new BorderLayout());
		this.service = service;
		this.dataChanged = dataChanged;
		this.confirmer = confirmer;
		add(UiComponents.page(null, content), BorderLayout.CENTER);
		showMenu();
	}

	void refreshPending()
	{
		// Staff drop review happens in Discord. The RuneLite staff page only shows working controls.
	}

	void showMenu()
	{
		content.removeAll();
		content.add(UiComponents.heading("Staff"));
		JPanel menu = UiComponents.card("Menu", "", "", RancourTheme.BRAND_RED_MUTED);
		announcementsButton = UiComponents.neutralButton("Announcements");
		dropsPanelButton = UiComponents.neutralButton("Drops Panel");
		teamsButton = UiComponents.neutralButton("Teams");
		announcementsButton.addActionListener(event -> showAnnouncementPage());
		dropsPanelButton.addActionListener(event -> showDropsPanelPage());
		teamsButton.addActionListener(event -> showTeamsPage());
		menu.add(announcementsButton);
		menu.add(dropsPanelButton);
		menu.add(teamsButton);
		content.add(menu);
		content.add(status);
		refreshContent();
	}

	void showAnnouncementPage()
	{
		content.removeAll();
		content.add(UiComponents.heading("Announcements"));
		JButton back = UiComponents.neutralButton("Back");
		back.addActionListener(event -> showMenu());
		content.add(back);
		content.add(createAnnouncementForm());
		content.add(status);
		loadAnnouncements();
		refreshContent();
	}

	private JPanel createAnnouncementForm()
	{
		JPanel card = UiComponents.card("Create", "", "", RancourTheme.SUCCESS);
		JTextField title = UiComponents.compact(new JTextField());
		JTextArea message = new JTextArea(3, 12);
		announcementTitle = title;
		announcementMessage = message;
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		UiComponents.compact(message);
		JComboBox<String> priority = UiComponents.compact(new JComboBox<>(new String[] {"normal", "high", "urgent"}));
		JComboBox<ExpiryOption> expiry = UiComponents.compact(new JComboBox<>(ExpiryOption.OPTIONS));
		JButton create = UiComponents.successButton("Create");
		createAnnouncementButton = create;
		create.addActionListener(event ->
		{
			if (title.getText().trim().isEmpty() || message.getText().trim().isEmpty())
			{
				status.setText("Title and body are required");
				return;
			}
			create.setEnabled(false);
			status.setText("Creating...");
			ExpiryOption selectedExpiry = (ExpiryOption) expiry.getSelectedItem();
			service.createAnnouncement(new CreateAnnouncementRequest(title.getText().trim(), message.getText().trim(),
				(String) priority.getSelectedItem(), selectedExpiry == null ? null : selectedExpiry.expiresAt()))
				.whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
					onAnnouncementCreated(title, message, priority, expiry, create, result, error)));
		});
		card.add(UiComponents.wrapped("Title"));
		card.add(title);
		card.add(UiComponents.wrapped("Body"));
		card.add(message);
		card.add(UiComponents.wrapped("Priority"));
		card.add(priority);
		card.add(UiComponents.wrapped("Expiry"));
		card.add(expiry);
		card.add(create);
		return card;
	}

	private void loadAnnouncements()
	{
		status.setText("Loading...");
		service.loadAnnouncements().whenComplete((items, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				return;
			}
			renderAnnouncementList(items);
		}));
	}

	private void renderAnnouncementList(List<Announcement> items)
	{
		if (items == null || items.isEmpty())
		{
			content.add(UiComponents.card("Existing Announcements", "No active announcements.", "", RancourTheme.DISABLED));
			status.setText("No announcements");
		}
		else
		{
			status.setText(items.size() + " active");
			content.add(UiComponents.heading("Existing Announcements"));
			for (Announcement item : items)
			{
				JPanel card = UiComponents.detailsCard(item.getTitle(), preview(item.getMessage()), announcementAccent(item.getPriority()));
				JButton delete = UiComponents.dangerButton("Delete");
				delete.addActionListener(event -> deleteAnnouncement(item));
				card.add(delete);
				content.add(card);
			}
		}
		refreshContent();
	}

	private void deleteAnnouncement(Announcement item)
	{
		if (!confirm("Delete this announcement?", true))
		{
			return;
		}
		status.setText("Deleting...");
		service.deleteAnnouncement(item.getId()).whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				return;
			}
			status.setText(result.getMessage());
			dataChanged.run();
			showAnnouncementPage();
		}));
	}

	private void showDropsPanelPage()
	{
		content.removeAll();
		content.add(UiComponents.heading("Drops Panel"));
		JButton back = UiComponents.neutralButton("Back");
		back.addActionListener(event -> showMenu());
		content.add(back);
		renderDropsPanelCard();
		content.add(status);
		refreshContent();
	}

	private void renderDropsPanelCard()
	{
		JPanel card = UiComponents.card("State", dropsPanelEnabled ? "Enabled" : "Disabled", "",
			dropsPanelEnabled ? RancourTheme.SUCCESS : RancourTheme.DANGER);
		card.add(UiComponents.badge(dropsPanelEnabled ? "ENABLED" : "DISABLED",
			dropsPanelEnabled ? RancourTheme.SUCCESS : RancourTheme.DANGER));
		JButton toggle = dropsPanelEnabled
			? UiComponents.dangerButton("Disable Drops Panel")
			: UiComponents.successButton("Enable Drops Panel");
		toggle.addActionListener(event -> toggleDropsPanel(!dropsPanelEnabled));
		card.add(toggle);
		content.add(card);
	}

	private void toggleDropsPanel(boolean enabled)
	{
		String message = enabled ? "Enable drop submissions for members?" : "Disable drop submissions for members?";
		if (!confirm(message, !enabled))
		{
			return;
		}
		status.setText("Saving...");
		service.setDropsPanelEnabled(enabled).whenComplete((settings, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				return;
			}
			applySettings(settings);
			dataChanged.run();
			showDropsPanelPage();
		}));
	}

	private void showTeamsPage()
	{
		content.removeAll();
		content.add(UiComponents.heading("Teams"));
		JButton back = UiComponents.neutralButton("Back");
		back.addActionListener(event -> showMenu());
		content.add(back);
		content.add(status);
		status.setText("Loading teams...");
		service.loadTeams().whenComplete((items, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				refreshContent();
				return;
			}
			renderTeams(items);
		}));
		refreshContent();
	}

	private void renderTeams(List<Team> teams)
	{
		if (teams == null || teams.isEmpty())
		{
			content.add(UiComponents.card("Active Teams", "No active teams.", "", RancourTheme.DISABLED));
			status.setText("No teams");
		}
		else
		{
			status.setText(teams.size() + " team(s)");
			for (Team team : teams)
			{
				JPanel card = UiComponents.detailsCard(team.getActivity(), "", teamAccent(team),
					"Host", team.getHost(),
					"World", String.valueOf(team.getWorld()),
					"Members", team.getCurrentMembers() + "/" + team.getCapacity(),
					"Joined", joinedMembers(team),
					"Expires", UiComponents.shortDate(team.getExpiresAt()));
				JButton edit = UiComponents.neutralButton("Edit");
				JButton close = UiComponents.dangerButton("Close");
				edit.addActionListener(event -> showEditTeamPage(team));
				close.addActionListener(event -> closeTeam(team));
				card.add(edit);
				card.add(close);
				content.add(card);
			}
		}
		refreshContent();
	}

	private void showEditTeamPage(Team team)
	{
		content.removeAll();
		content.add(UiComponents.heading("Edit Team"));
		JButton back = UiComponents.neutralButton("Back");
		back.addActionListener(event -> showTeamsPage());
		content.add(back);
		JPanel card = UiComponents.card("Team", "", "", RancourTheme.INFO);
		JTextField activity = UiComponents.compact(new JTextField(team.getActivity()));
		JTextField capacity = UiComponents.compact(new JTextField(String.valueOf(team.getCapacity())));
		JTextField world = UiComponents.compact(new JTextField(String.valueOf(team.getWorld())));
		JCheckBox voice = UiComponents.compact(new JCheckBox("Voice required", team.isVoiceRequired()));
		voice.setOpaque(false);
		JTextField tags = UiComponents.compact(new JTextField(String.join(", ", team.getTags())));
		JComboBox<String> statusBox = UiComponents.compact(new JComboBox<>(new String[] {"open", "closed"}));
		statusBox.setSelectedItem(team.getStatus());
		JButton save = UiComponents.successButton("Save");
		save.addActionListener(event -> saveTeam(team, activity, capacity, world, voice, tags, statusBox));
		card.add(UiComponents.wrapped("Activity"));
		card.add(activity);
		card.add(UiComponents.wrapped("Capacity"));
		card.add(capacity);
		card.add(UiComponents.wrapped("World"));
		card.add(world);
		card.add(voice);
		card.add(UiComponents.wrapped("Tags"));
		card.add(tags);
		card.add(UiComponents.wrapped("Status"));
		card.add(statusBox);
		card.add(save);
		content.add(card);
		content.add(status);
		refreshContent();
	}

	private void saveTeam(Team team, JTextField activity, JTextField capacity, JTextField world,
		JCheckBox voice, JTextField tags, JComboBox<String> statusBox)
	{
		Integer capacityValue = parseInt(capacity.getText(), "Capacity");
		Integer worldValue = parseInt(world.getText(), "World");
		if (capacityValue == null || worldValue == null)
		{
			return;
		}
		status.setText("Saving team...");
		TeamEditRequest request = new TeamEditRequest(activity.getText().trim(), capacityValue, worldValue,
			voice.isSelected(), splitCsv(tags.getText()), (String) statusBox.getSelectedItem());
		service.editTeam(team.getId(), request).whenComplete((updated, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				return;
			}
			status.setText("Saved: " + updated.getActivity());
			dataChanged.run();
			showTeamsPage();
		}));
	}

	private void closeTeam(Team team)
	{
		if (!confirm("Close this team?", true))
		{
			return;
		}
		status.setText("Closing team...");
		service.closeTeam(team.getId()).whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				return;
			}
			status.setText(result.getMessage());
			dataChanged.run();
			showTeamsPage();
		}));
	}

	private void onAnnouncementCreated(
		JTextField title,
		JTextArea message,
		JComboBox<String> priority,
		JComboBox<ExpiryOption> expiry,
		JButton create,
		Announcement result,
		Throwable error)
	{
		create.setEnabled(true);
		if (error != null)
		{
			status.setText("Error: " + UiComponents.errorMessage(error));
			return;
		}
		title.setText("");
		message.setText("");
		priority.setSelectedItem("normal");
		expiry.setSelectedIndex(0);
		status.setText("Created: " + result.getTitle());
		dataChanged.run();
		showAnnouncementPage();
	}

	void applySettings(PluginSettings settings)
	{
		if (settings != null)
		{
			dropsPanelEnabled = settings.isDropsPanelEnabled();
		}
	}

	private boolean confirm(String message)
	{
		return confirm(message, false);
	}

	private boolean confirm(String message, boolean danger)
	{
		if (confirmer != null)
		{
			return confirmer.test(message);
		}
		return JOptionPane.showConfirmDialog(this, message, danger ? "Confirm destructive action" : "Confirm",
			JOptionPane.YES_NO_OPTION, danger ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE)
			== JOptionPane.YES_OPTION;
	}

	private void refreshContent()
	{
		content.revalidate();
		content.repaint();
	}

	private static String preview(String value)
	{
		String text = UiComponents.value(value);
		return text.length() <= 120 ? text : text.substring(0, 117) + "...";
	}

	private Integer parseInt(String value, String label)
	{
		try
		{
			return Integer.parseInt(value.trim());
		}
		catch (NumberFormatException error)
		{
			status.setText(label + " must be a number");
			return null;
		}
	}

	private static List<String> splitCsv(String value)
	{
		java.util.ArrayList<String> result = new java.util.ArrayList<>();
		for (String item : UiComponents.value(value).split(","))
		{
			String trimmed = item.trim();
			if (!trimmed.isEmpty())
			{
				result.add(trimmed);
			}
		}
		return result;
	}

	private static String joinedMembers(Team team)
	{
		return team.getJoinedMembers().isEmpty() ? "None" : String.join(", ", team.getJoinedMembers());
	}

	private static java.awt.Color announcementAccent(String priority)
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

	private static java.awt.Color teamAccent(Team team)
	{
		if ("closed".equalsIgnoreCase(team.getStatus()) || "expired".equalsIgnoreCase(team.getStatus()))
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
		return team.isStaffHosted() ? RancourTheme.BRAND_RED : RancourTheme.SUCCESS;
	}

	private static final class ExpiryOption
	{
		private static final ExpiryOption[] OPTIONS = {
			new ExpiryOption("1 hour", 1, ChronoUnit.HOURS),
			new ExpiryOption("6 hours", 6, ChronoUnit.HOURS),
			new ExpiryOption("12 hours", 12, ChronoUnit.HOURS),
			new ExpiryOption("1 day", 1, ChronoUnit.DAYS),
			new ExpiryOption("2 days", 2, ChronoUnit.DAYS),
			new ExpiryOption("3 days", 3, ChronoUnit.DAYS),
			new ExpiryOption("7 days", 7, ChronoUnit.DAYS)
		};
		private final String label;
		private final int amount;
		private final ChronoUnit unit;

		private ExpiryOption(String label, int amount, ChronoUnit unit)
		{
			this.label = label;
			this.amount = amount;
			this.unit = unit;
		}

		private String expiresAt()
		{
			return Instant.now().plus(amount, unit).truncatedTo(ChronoUnit.SECONDS).toString();
		}

		@Override
		public String toString()
		{
			return label;
		}
	}
}
