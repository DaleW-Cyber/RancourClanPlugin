package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import com.rancour.clan.models.Announcement;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.services.StaffService;

final class StaffPanel extends JPanel
{
	private final StaffService service;
	private final Runnable announcementCreated;
	private final JTextArea status = UiComponents.statusLabel("Staff tools ready");
	private final JPanel content = UiComponents.contentPanel();
	JButton announcementsButton;
	JTextField announcementTitle;
	JTextArea announcementMessage;
	JButton createAnnouncementButton;

	StaffPanel(StaffService service)
	{
		this(service, () -> { });
	}

	StaffPanel(StaffService service, Runnable announcementCreated)
	{
		super(new BorderLayout());
		this.service = service;
		this.announcementCreated = announcementCreated;
		add(UiComponents.page(null, content), BorderLayout.CENTER);
		showMenu();
	}

	void refreshPending()
	{
		// RuneLite pending-drop review is intentionally hidden. Staff review happens in Discord.
	}

	void showMenu()
	{
		content.removeAll();
		content.add(UiComponents.heading("Staff"));
		JPanel menu = UiComponents.card("Tools", "", "");
		announcementsButton = new JButton("Announcements");
		announcementsButton.addActionListener(event -> showAnnouncementPage());
		menu.add(announcementsButton);
		content.add(menu);
		content.add(status);
		refreshContent();
	}

	void showAnnouncementPage()
	{
		content.removeAll();
		content.add(UiComponents.heading("Announcements"));
		JButton back = new JButton("Back");
		back.addActionListener(event -> showMenu());
		content.add(back);
		content.add(createAnnouncementForm());
		content.add(status);
		refreshContent();
	}

	private JPanel createAnnouncementForm()
	{
		JPanel card = UiComponents.card("Create", "", "");
		JTextField title = new JTextField();
		JTextArea message = new JTextArea(3, 12);
		announcementTitle = title;
		announcementMessage = message;
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		JComboBox<String> priority = new JComboBox<>(new String[] {"normal", "high", "urgent"});
		JComboBox<ExpiryOption> expiry = new JComboBox<>(ExpiryOption.OPTIONS);
		JButton create = new JButton("Create");
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
				.whenComplete((result, error) -> SwingUtilities.invokeLater(() -> onAnnouncementCreated(
					title, message, priority, expiry, create, result, error)));
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
		announcementCreated.run();
	}

	private void refreshContent()
	{
		content.revalidate();
		content.repaint();
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
