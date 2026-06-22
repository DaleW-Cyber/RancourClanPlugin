package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.PluginSettings;
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
		JPanel menu = UiComponents.card("Menu", "", "");
		announcementsButton = UiComponents.compact(new JButton("Announcements"));
		dropsPanelButton = UiComponents.compact(new JButton("Drops Panel"));
		announcementsButton.addActionListener(event -> showAnnouncementPage());
		dropsPanelButton.addActionListener(event -> showDropsPanelPage());
		menu.add(announcementsButton);
		menu.add(dropsPanelButton);
		content.add(menu);
		content.add(status);
		refreshContent();
	}

	void showAnnouncementPage()
	{
		content.removeAll();
		content.add(UiComponents.heading("Announcements"));
		JButton back = UiComponents.compact(new JButton("Back"));
		back.addActionListener(event -> showMenu());
		content.add(back);
		content.add(createAnnouncementForm());
		content.add(status);
		loadAnnouncements();
		refreshContent();
	}

	private JPanel createAnnouncementForm()
	{
		JPanel card = UiComponents.card("Create", "", "");
		JTextField title = UiComponents.compact(new JTextField());
		JTextArea message = new JTextArea(3, 12);
		announcementTitle = title;
		announcementMessage = message;
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		UiComponents.compact(message);
		JComboBox<String> priority = UiComponents.compact(new JComboBox<>(new String[] {"normal", "high", "urgent"}));
		JComboBox<ExpiryOption> expiry = UiComponents.compact(new JComboBox<>(ExpiryOption.OPTIONS));
		JButton create = UiComponents.compact(new JButton("Create"));
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
			content.add(UiComponents.card("Existing Announcements", "No active announcements.", ""));
			status.setText("No announcements");
		}
		else
		{
			status.setText(items.size() + " active");
			content.add(UiComponents.heading("Existing Announcements"));
			for (Announcement item : items)
			{
				JPanel card = UiComponents.detailsCard(item.getTitle(), preview(item.getMessage()));
				JButton delete = UiComponents.compact(new JButton("Delete"));
				delete.addActionListener(event -> deleteAnnouncement(item));
				card.add(delete);
				content.add(card);
			}
		}
		refreshContent();
	}

	private void deleteAnnouncement(Announcement item)
	{
		if (!confirm("Delete this announcement?"))
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
		JButton back = UiComponents.compact(new JButton("Back"));
		back.addActionListener(event -> showMenu());
		content.add(back);
		renderDropsPanelCard();
		content.add(status);
		refreshContent();
	}

	private void renderDropsPanelCard()
	{
		JPanel card = UiComponents.card("State", dropsPanelEnabled ? "Enabled" : "Disabled", "");
		JButton toggle = UiComponents.compact(new JButton(dropsPanelEnabled ? "Disable Drops Panel" : "Enable Drops Panel"));
		toggle.addActionListener(event -> toggleDropsPanel(!dropsPanelEnabled));
		card.add(toggle);
		content.add(card);
	}

	private void toggleDropsPanel(boolean enabled)
	{
		String message = enabled ? "Enable drop submissions for members?" : "Disable drop submissions for members?";
		if (!confirm(message))
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
		if (confirmer != null)
		{
			return confirmer.test(message);
		}
		return JOptionPane.showConfirmDialog(this, message, "Confirm", JOptionPane.YES_NO_OPTION)
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
