package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.rancour.clan.models.DropCandidate;
import com.rancour.clan.services.DropService;

final class DropsPanel extends JPanel
{
	private final DropService service;
	private final JLabel status = UiComponents.statusLabel("Waiting for a candidate drop");
	private final JPanel content = UiComponents.contentPanel();
	private DropCandidate candidate;

	DropsPanel(DropService service)
	{
		super(new BorderLayout());
		this.service = service;
		content.add(UiComponents.heading("Drops"));
		content.add(UiComponents.card("Confirmation required",
			"Candidate drops detected from game chat appear here. Nothing is submitted automatically.",
			"Duplicate detections are suppressed briefly"));
		JPanel controls = new JPanel(new BorderLayout());
		controls.add(status, BorderLayout.CENTER);
		add(UiComponents.page(controls, content), BorderLayout.CENTER);
	}

	void offerCandidate(DropCandidate newCandidate)
	{
		candidate = newCandidate;
		content.removeAll();
		content.add(UiComponents.heading("Drops"));
		JPanel card = UiComponents.detailsCard(newCandidate.getItemName(), "Review this candidate before submitting.",
			"Source", newCandidate.getSource(),
			"RSN", newCandidate.getRsn(),
			"Detected", newCandidate.getDetectedAt(),
			"Method", newCandidate.getDetectionMethod());
		JPanel actions = new JPanel(new GridLayout(1, 2, 4, 0));
		JButton confirm = new JButton("Confirm Submit");
		JButton dismiss = new JButton("Dismiss");
		confirm.addActionListener(event -> submit());
		dismiss.addActionListener(event -> clear("Candidate dismissed"));
		actions.add(confirm);
		actions.add(dismiss);
		card.add(actions);
		content.add(card);
		status.setText("Review candidate before submitting");
		content.revalidate();
		content.repaint();
	}

	private void submit()
	{
		DropCandidate pending = candidate;
		if (pending == null)
		{
			return;
		}
		status.setText("Submitting confirmed drop...");
		service.submit(pending.toSubmission()).whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				return;
			}
			clear("Submitted: " + result.getStatus() + " - " + result.getMessage());
		}));
	}

	private void clear(String message)
	{
		candidate = null;
		content.removeAll();
		content.add(UiComponents.heading("Drops"));
		content.add(UiComponents.wrapped("No candidate drop is awaiting confirmation."));
		status.setText(message);
		content.revalidate();
		content.repaint();
	}
}
