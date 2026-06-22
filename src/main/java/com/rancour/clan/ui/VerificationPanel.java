package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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
	private final JLabel status = UiComponents.statusLabel("Not checked");
	private final JPanel content = UiComponents.contentPanel();
	private final ClipboardWriter clipboard;

	VerificationPanel(VerificationService service)
	{
		this(service, value -> Toolkit.getDefaultToolkit().getSystemClipboard()
			.setContents(new StringSelection(value), null));
	}

	VerificationPanel(VerificationService service, ClipboardWriter clipboard)
	{
		super(new BorderLayout());
		this.service = service;
		this.clipboard = clipboard;
		JButton generate = new JButton("Generate Link Code");
		JButton refresh = new JButton("Refresh Status");
		JButton testConnection = new JButton("Test API Connection");
		generate.addActionListener(event -> generateCode());
		refresh.addActionListener(event -> refresh());
		testConnection.addActionListener(event -> testConnection());
		JPanel buttons = new JPanel(new GridLayout(3, 1, 4, 4));
		buttons.add(generate);
		buttons.add(refresh);
		buttons.add(testConnection);
		JPanel controls = new JPanel(new BorderLayout(0, 4));
		controls.add(buttons, BorderLayout.CENTER);
		controls.add(status, BorderLayout.SOUTH);
		content.add(UiComponents.heading("Verification"));
		content.add(UiComponents.card("Clan account", "Generate a short-lived code, then use /plugin_link in Discord.", "No Discord token is stored in RuneLite"));
		add(UiComponents.page(controls, content), BorderLayout.CENTER);
		service.addProfileListener(profile -> SwingUtilities.invokeLater(() -> showProfile(profile)));
	}

	private void testConnection()
	{
		setStatus("Testing API connection...");
		service.testConnection().whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				setStatus("API test failed: " + UiComponents.errorMessage(error));
				return;
			}
			setStatus("API connection successful: " + UiComponents.value(result.getStatus()));
		}));
	}

	void refresh()
	{
		setStatus("Checking verification...");
		service.refreshStatus().whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				setStatus("Error: " + UiComponents.errorMessage(error));
				return;
			}
			setStatus(result.isVerified() ? "Verified" : "Status: " + UiComponents.value(result.getState()));
			if (result.getProfile() != null)
			{
				showProfile(result.getProfile());
			}
		}));
	}

	private void generateCode()
	{
		setStatus("Generating code...");
		service.generateLinkCode().whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null)
			{
				setStatus("Error: " + UiComponents.errorMessage(error));
				return;
			}
			setStatus("Code generated");
			content.removeAll();
			content.add(UiComponents.heading("Verification"));
			JPanel card = UiComponents.detailsCard("Link code", "Use this short-lived code in Discord.",
				"Code", result.getCode(),
				"Discord command", "/plugin_link " + result.getCode(),
				"Expires", result.getExpiresAt());
			JButton copy = new JButton("Copy Code");
			copy.addActionListener(event -> copyCode(result.getCode()));
			card.add(copy);
			content.add(card);
			content.revalidate();
			content.repaint();
		}));
	}

	private void showProfile(MemberProfile profile)
	{
		content.removeAll();
		content.add(UiComponents.heading("Verification"));
		content.add(UiComponents.detailsCard("Verified member", "Your RuneLite session is linked to Discord.",
			"Discord", profile.getDiscordName(),
			"RSN", profile.getRsn(),
			"Clan rank", profile.getClanRank(),
			"Staff", profile.isStaff() ? "Yes" : "No",
			"Expires", profile.getExpiresAt(),
			"Last checked", profile.getLastCheckedAt()));
		content.revalidate();
		content.repaint();
	}

	private void copyCode(String code)
	{
		try
		{
			clipboard.copy(code);
			setStatus("Copied to clipboard");
		}
		catch (RuntimeException error)
		{
			setStatus("Could not copy code to clipboard");
		}
	}

	private void setStatus(String message)
	{
		status.setText(message);
	}
}
