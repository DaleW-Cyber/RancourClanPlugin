package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.VerificationStatus;
import com.rancour.clan.services.VerificationService;

final class VerificationPanel extends JPanel
{
	private final VerificationService service;
	private final JTextArea status = UiComponents.statusLabel("Not checked");
	private final JPanel content = UiComponents.contentPanel();
	private final ClipboardWriter clipboard;
	private final Supplier<String> activeRsn;
	private boolean loading;

	VerificationPanel(VerificationService service)
	{
		this(service, () -> "", value -> Toolkit.getDefaultToolkit().getSystemClipboard()
			.setContents(new StringSelection(value), null));
	}

	VerificationPanel(VerificationService service, ClipboardWriter clipboard)
	{
		this(service, () -> "", clipboard);
	}

	VerificationPanel(VerificationService service, Supplier<String> activeRsn)
	{
		this(service, activeRsn, value -> Toolkit.getDefaultToolkit().getSystemClipboard()
			.setContents(new StringSelection(value), null));
	}

	VerificationPanel(VerificationService service, Supplier<String> activeRsn, ClipboardWriter clipboard)
	{
		super(new BorderLayout());
		this.service = service;
		this.activeRsn = activeRsn;
		this.clipboard = clipboard;
		JButton generate = new JButton("Generate Code");
		JButton refresh = new JButton("Refresh");
		JButton testConnection = new JButton("Test API");
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
		content.add(UiComponents.card("Link account", "Use /plugin_link in Discord.", ""));
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
		if (loading)
		{
			return;
		}
		loading = true;
		setStatus("Checking verification...");
		service.refreshStatus().whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
		{
			loading = false;
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
			JPanel card = UiComponents.detailsCard("Link code", "",
				"Code", result.getCode(),
				"Discord", "/plugin_link " + result.getCode(),
				"Expires", UiComponents.shortDate(result.getExpiresAt()));
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
		String currentRsn = UiComponents.value(activeRsn.get()).trim();
		String active = currentRsn.isEmpty() ? "Log in to confirm active RSN." : currentRsn;
		String warning = currentRsn.isEmpty() || profile.isLinkedRsn(currentRsn)
			? ""
			: "Warning: this RuneLite account is not linked to your Discord profile.";
		content.removeAll();
		content.add(UiComponents.heading("Verification"));
		content.add(UiComponents.detailsCard("Verified", "",
			"Discord", profile.getDiscordName(),
			"Active RSN", active,
			"Linked RSNs", String.join(", ", profile.getLinkedRsns()),
			"Clan rank", profile.getClanRank(),
			"Staff", profile.isStaff() ? "Yes" : "No",
			"Expires", UiComponents.shortDate(profile.getExpiresAt()),
			"Last checked", UiComponents.shortDate(profile.getLastCheckedAt())));
		if (!warning.isEmpty())
		{
			content.add(UiComponents.card("Account not linked", warning, "Drop submission is disabled."));
		}
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
