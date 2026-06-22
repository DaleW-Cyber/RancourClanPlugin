package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.VerificationStatus;
import com.rancour.clan.services.VerificationService;

final class VerificationPanel extends JPanel
{
	private final VerificationService service;
	private final JLabel status = new JLabel("Not checked");
	private final JPanel content = UiComponents.contentPanel();

	VerificationPanel(VerificationService service)
	{
		super(new BorderLayout());
		this.service = service;
		JButton generate = new JButton("Generate Link Code");
		JButton refresh = new JButton("Refresh Status");
		generate.addActionListener(event -> generateCode());
		refresh.addActionListener(event -> refresh());
		JPanel controls = new JPanel(new GridLayout(3, 1, 4, 4));
		controls.add(generate);
		controls.add(refresh);
		controls.add(status);
		content.add(UiComponents.heading("Verification"));
		content.add(UiComponents.card("Clan account", "Generate a short-lived code, then use /plugin_link in Discord.", "No Discord token is stored in RuneLite"));
		add(UiComponents.page(controls, content), BorderLayout.CENTER);
		service.addProfileListener(profile -> SwingUtilities.invokeLater(() -> showProfile(profile)));
	}

	void refresh()
	{
		status.setText("Checking verification...");
		service.refreshStatus().whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				return;
			}
			status.setText(result.isVerified() ? "Verified" : "Status: " + UiComponents.value(result.getState()));
			if (result.getProfile() != null)
			{
				showProfile(result.getProfile());
			}
		}));
	}

	private void generateCode()
	{
		status.setText("Generating code...");
		service.generateLinkCode().whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				status.setText("Error: " + UiComponents.errorMessage(error));
				return;
			}
			status.setText("Code generated");
			content.add(UiComponents.card("Link code: " + result.getCode(),
				"Use /plugin_link " + result.getCode() + " in Discord.", "Expires: " + result.getExpiresAt()));
			content.revalidate();
			content.repaint();
		}));
	}

	private void showProfile(MemberProfile profile)
	{
		content.removeAll();
		content.add(UiComponents.heading("Verification"));
		String details = "Discord: " + profile.getDiscordName() + " | RSN: " + profile.getRsn()
			+ " | Clan rank: " + profile.getClanRank() + " | Staff: " + (profile.isStaff() ? "Yes" : "No");
		content.add(UiComponents.card("Verified member", details,
			"Expires: " + profile.getExpiresAt() + " | Checked: " + profile.getLastCheckedAt()));
		content.revalidate();
		content.repaint();
	}
}
