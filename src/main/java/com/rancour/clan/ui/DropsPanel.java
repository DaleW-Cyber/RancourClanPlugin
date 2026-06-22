package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import com.rancour.clan.models.DropCandidate;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.services.DropService;

final class DropsPanel extends JPanel
{
	private final DropService service;
	private final JTextArea status = UiComponents.statusLabel("Waiting for a candidate drop");
	private final JPanel content = UiComponents.contentPanel();
	private final Supplier<String> activeRsn;
	private DropCandidate candidate;
	private MemberProfile profile;

	DropsPanel(DropService service)
	{
		this(service, () -> "");
	}

	DropsPanel(DropService service, Supplier<String> activeRsn)
	{
		super(new BorderLayout());
		this.service = service;
		this.activeRsn = activeRsn;
		content.add(UiComponents.heading("Drops"));
		content.add(UiComponents.card("Confirm drops", "Detected drops appear here.", ""));
		JPanel controls = new JPanel(new BorderLayout());
		controls.add(status, BorderLayout.CENTER);
		add(UiComponents.page(controls, content), BorderLayout.CENTER);
	}

	void offerCandidate(DropCandidate newCandidate)
	{
		candidate = newCandidate;
		String currentRsn = UiComponents.value(activeRsn.get()).trim();
		boolean loggedIn = !currentRsn.isEmpty();
		boolean linked = loggedIn && profile != null && profile.isLinkedRsn(currentRsn);
		content.removeAll();
		content.add(UiComponents.heading("Drops"));
		JPanel card = UiComponents.detailsCard(newCandidate.getItemName(), "",
			"Source", newCandidate.getSource(),
			"RSN", newCandidate.getRsn(),
			"Detected", UiComponents.shortDate(newCandidate.getDetectedAt()),
			"Method", newCandidate.getDetectionMethod());
		if (!loggedIn)
		{
			card.add(UiComponents.wrapped("Log in to confirm active RSN."));
		}
		else if (!linked)
		{
			card.add(UiComponents.wrapped("This RuneLite account is not linked to your Discord profile."));
		}
		JPanel actions = new JPanel(new GridLayout(2, 1, 0, 4));
		JButton confirm = new JButton("Confirm Submit");
		JButton dismiss = new JButton("Dismiss");
		confirm.addActionListener(event -> submit());
		dismiss.addActionListener(event -> clear("Candidate dismissed"));
		actions.add(confirm);
		actions.add(dismiss);
		confirm.setEnabled(linked);
		card.add(actions);
		content.add(card);
		status.setText(linked ? "Review candidate before submitting" : "Drop submission is disabled");
		content.revalidate();
		content.repaint();
	}

	void setProfile(MemberProfile profile)
	{
		this.profile = profile;
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
