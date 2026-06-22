package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import com.rancour.clan.models.ActionResult;
import com.rancour.clan.models.CreateAnnouncementRequest;
import com.rancour.clan.models.StaffDropSubmission;
import com.rancour.clan.services.StaffService;

final class StaffPanel extends JPanel
{
	private final StaffService service;
	private final JTextArea status = UiComponents.statusLabel("Staff tools ready");
	private final JPanel pending = UiComponents.contentPanel();

	StaffPanel(StaffService service)
	{
		super(new BorderLayout());
		this.service = service;
		JPanel content = UiComponents.contentPanel();
		content.add(UiComponents.heading("Staff Administration"));
		content.add(createAnnouncementForm());
		content.add(eventCacheControls());
		content.add(teamContractNotice());
		content.add(pending);
		JButton refresh = new JButton("Refresh Pending Drops");
		refresh.addActionListener(event -> refreshPending());
		JPanel controls = new JPanel(new BorderLayout());
		controls.add(refresh, BorderLayout.CENTER);
		controls.add(status, BorderLayout.SOUTH);
		add(UiComponents.page(controls, content), BorderLayout.CENTER);
	}

	void refreshPending()
	{
		status.setText("Loading pending drops...");
		service.loadPendingDrops().whenComplete((items, error) -> SwingUtilities.invokeLater(() -> renderPending(items, error)));
	}

	private JPanel createAnnouncementForm()
	{
		JPanel card = UiComponents.card("Create announcement", "Publishes through the Rancour API only.", "Expiry accepts the Railway API's ISO-8601 format");
		JTextField title = new JTextField();
		JTextArea message = new JTextArea(4, 12);
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		JComboBox<String> priority = new JComboBox<>(new String[] {"normal", "high", "urgent"});
		JTextField expiry = new JTextField();
		JButton create = new JButton("Create Announcement");
		create.addActionListener(event ->
		{
			if (title.getText().trim().isEmpty() || message.getText().trim().isEmpty())
			{
				status.setText("Title and message are required");
				return;
			}
			status.setText("Creating announcement...");
			service.createAnnouncement(new CreateAnnouncementRequest(title.getText().trim(), message.getText().trim(),
				(String) priority.getSelectedItem(), expiry.getText().trim())).whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
				status.setText(error == null ? "Announcement created: " + result.getTitle() : "Error: " + UiComponents.errorMessage(error))));
		});
		card.add(UiComponents.wrapped("Title"));
		card.add(title);
		card.add(UiComponents.wrapped("Message"));
		card.add(message);
		card.add(UiComponents.wrapped("Priority"));
		card.add(priority);
		card.add(UiComponents.wrapped("Expires at (optional)"));
		card.add(expiry);
		card.add(create);
		return card;
	}

	private JPanel eventCacheControls()
	{
		JPanel card = UiComponents.card("Event cache", "Request a refresh of the Discord-event cache.", "Requires a future Railway API endpoint");
		JButton refresh = new JButton("Refresh Event Cache");
		refresh.addActionListener(event -> action(service.refreshEventCache()));
		card.add(refresh);
		return card;
	}

	private JPanel teamContractNotice()
	{
		JPanel card = UiComponents.card("Team moderation", "Close and lock actions are typed in the client API but disabled until backend routes are agreed.", "No endpoint in the current API contract");
		JPanel actions = new JPanel(new GridLayout(2, 1, 0, 4));
		JButton close = new JButton("Close Team");
		JButton lock = new JButton("Lock Team");
		close.setEnabled(false);
		lock.setEnabled(false);
		actions.add(close);
		actions.add(lock);
		card.add(actions);
		return card;
	}

	private void renderPending(List<StaffDropSubmission> items, Throwable error)
	{
		pending.removeAll();
		pending.add(UiComponents.heading("Pending Drops"));
		if (error != null)
		{
			status.setText("Error: " + UiComponents.errorMessage(error));
			pending.add(UiComponents.wrapped("Pending submissions could not be loaded."));
		}
		else if (items == null || items.isEmpty())
		{
			status.setText("No pending drops");
			pending.add(UiComponents.wrapped("The review queue is empty."));
		}
		else
		{
			status.setText(items.size() + " pending drop(s)");
			for (StaffDropSubmission item : items)
			{
				JPanel card = UiComponents.detailsCard(item.getItemName(), "",
					"Source", item.getSource(),
					"RSN", item.getRsn(),
					"Submitted", UiComponents.shortDate(item.getSubmittedAt()),
					"Status", item.getStatus());
				JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 4));
				JButton approve = new JButton("Approve");
				JButton reject = new JButton("Reject");
				approve.addActionListener(event -> action(service.approveDrop(item.getId())));
				reject.addActionListener(event -> action(service.rejectDrop(item.getId())));
				buttons.add(approve);
				buttons.add(reject);
				card.add(buttons);
				pending.add(card);
			}
		}
		pending.revalidate();
		pending.repaint();
	}

	private void action(CompletionStage<ActionResult> action)
	{
		status.setText("Saving staff action...");
		action.whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			status.setText(error == null ? result.getMessage() : "Error: " + UiComponents.errorMessage(error));
			if (error == null) { refreshPending(); }
		}));
	}
}
