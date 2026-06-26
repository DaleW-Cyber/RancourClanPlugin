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
import com.rancour.clan.models.PluginSettings;
import com.rancour.clan.services.DropService;

final class DropsPanel extends JPanel
{
	private final DropService service;
	private final JTextArea status = UiComponents.statusLabel("Waiting for a candidate drop");
	private final JPanel content = UiComponents.contentPanel();
	private final Supplier<String> activeRsn;
	private DropCandidate candidate;
	private MemberProfile profile;
	private boolean dropsVisible = true;
	private boolean dropsCanSubmit = true;
	private String restrictionMessage = "Drop submissions are currently disabled.";

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
		content.add(UiComponents.card("Confirm drops", "Detected drops appear here.", "", RancourTheme.INFO));
		JPanel controls = new JPanel(new BorderLayout());
		controls.add(status, BorderLayout.CENTER);
		add(UiComponents.page(controls, content), BorderLayout.CENTER);
	}

	void offerCandidate(DropCandidate newCandidate)
	{
		if (!dropsVisible)
		{
			showDisabled();
			return;
		}
		candidate = newCandidate;
		String currentRsn = UiComponents.value(activeRsn.get()).trim();
		boolean loggedIn = !currentRsn.isEmpty();
		boolean linked = loggedIn && profile != null && profile.isLinkedRsn(currentRsn);
		content.removeAll();
		content.add(UiComponents.heading("Drops"));
		JPanel card = UiComponents.detailsCard(newCandidate.getItemName(), "", RancourTheme.WARNING,
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
		JButton confirm = UiComponents.successButton("Confirm Submit");
		JButton dismiss = UiComponents.neutralButton("Dismiss");
		confirm.addActionListener(event -> submit());
		dismiss.addActionListener(event -> clear("Candidate dismissed"));
		actions.add(confirm);
		actions.add(dismiss);
		confirm.setEnabled(linked && dropsCanSubmit);
		card.add(actions);
		content.add(card);
		status.setText(linked && dropsCanSubmit ? "Review candidate before submitting" : restrictionMessage);
		status.setForeground(linked && dropsCanSubmit ? RancourTheme.WARNING : RancourTheme.DANGER);
		content.revalidate();
		content.repaint();
	}

	void setProfile(MemberProfile profile)
	{
		this.profile = profile;
	}

	void setDropsPanelEnabled(boolean enabled)
	{
		this.dropsVisible = enabled;
		this.dropsCanSubmit = enabled;
		if (!enabled)
		{
			showDisabled();
		}
		else if (candidate == null)
		{
			showIdle("Drop submissions enabled");
		}
	}

	void applySettings(PluginSettings settings)
	{
		if (settings == null)
		{
			return;
		}
		this.dropsVisible = settings.isDropsVisible();
		this.dropsCanSubmit = settings.canSubmitDrops();
		this.restrictionMessage = UiComponents.value(settings.getDropsRestrictionMessage()).isEmpty()
			? "Drop submissions are currently disabled."
			: settings.getDropsRestrictionMessage();
		if (!dropsVisible)
		{
			showDisabled();
		}
		else if (candidate == null)
		{
			showIdle("Waiting for a candidate drop");
		}
	}

	void showDisabled()
	{
		candidate = null;
		content.removeAll();
		content.add(UiComponents.heading("Drops"));
		content.add(UiComponents.card("Drops disabled", restrictionMessage, "", RancourTheme.DANGER));
		status.setText("Disabled");
		status.setForeground(RancourTheme.DANGER);
		content.revalidate();
		content.repaint();
	}

	private void showIdle(String message)
	{
		content.removeAll();
		content.add(UiComponents.heading("Drops"));
		content.add(UiComponents.card("No pending drop", "No candidate drop is awaiting confirmation.", "",
			RancourTheme.INFO));
		status.setText(message);
		status.setForeground(RancourTheme.MUTED);
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
		status.setForeground(RancourTheme.WARNING);
		service.submit(pending.toSubmission()).whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				status.setForeground(RancourTheme.DANGER);
				return;
			}
			clear("Submitted: " + result.getStatus() + " - " + result.getMessage());
		}));
	}

	private void clear(String message)
	{
		candidate = null;
		showIdle(message);
		status.setText(message);
		status.setForeground(message.startsWith("Submitted:") ? RancourTheme.SUCCESS : RancourTheme.MUTED);
	}
}
