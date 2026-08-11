package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import com.rancour.clan.models.DropCandidate;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.PluginSettings;
import com.rancour.clan.services.DropChatNotifier;
import com.rancour.clan.services.DropService;

final class DropsPanel extends JPanel
{
	private final DropService service;
	private final JTextArea status = UiComponents.statusLabel("Waiting for a candidate drop");
	private final JPanel content = UiComponents.contentPanel();
	private final Supplier<String> activeRsn;
	private final Deque<DropCandidate> pendingCandidates = new ArrayDeque<>();
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
		if (candidate != null)
		{
			pendingCandidates.addLast(newCandidate);
			updateCandidateStatus();
			return;
		}
		showCandidate(newCandidate, true);
	}

	private void showCandidate(DropCandidate newCandidate, boolean notify)
	{
		candidate = newCandidate;
		if (notify)
		{
			DropChatNotifier.notify("Rancour PvM: A drop is ready to submit. Check the Rancour PvM plugin panel.");
		}
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
		dismiss.addActionListener(event -> advanceCandidate("Candidate dismissed"));
		actions.add(confirm);
		actions.add(dismiss);
		confirm.setEnabled(linked && dropsCanSubmit);
		card.add(actions);
		content.add(card);
		status.setForeground(linked && dropsCanSubmit ? RancourTheme.WARNING : RancourTheme.DANGER);
		updateCandidateStatus();
		content.revalidate();
		content.repaint();
	}

	private void updateCandidateStatus()
	{
		if (candidate == null)
		{
			return;
		}
		String currentRsn = UiComponents.value(activeRsn.get()).trim();
		boolean linked = !currentRsn.isEmpty() && profile != null && profile.isLinkedRsn(currentRsn);
		if (!linked || !dropsCanSubmit)
		{
			status.setText(restrictionMessage);
			return;
		}
		int queued = pendingCandidates.size();
		status.setText(queued == 0
			? "Review candidate before submitting"
			: "Review candidate before submitting (" + queued + " more pending)");
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
		else
		{
			updateCandidateStatus();
		}
	}

	void showDisabled()
	{
		candidate = null;
		pendingCandidates.clear();
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
			DropChatNotifier.notify("Rancour PvM: Your drop has been submitted successfully.");
			advanceCandidate("Submitted: " + result.getStatus() + " - " + result.getMessage());
		}));
	}

	private void advanceCandidate(String message)
	{
		candidate = null;
		DropCandidate next = pendingCandidates.pollFirst();
		if (next != null)
		{
			showCandidate(next, false);
			return;
		}
		showIdle(message);
		status.setText(message);
		status.setForeground(message.startsWith("Submitted:") ? RancourTheme.SUCCESS : RancourTheme.MUTED);
	}
}
