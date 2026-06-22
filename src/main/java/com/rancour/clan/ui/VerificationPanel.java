package com.rancour.clan.ui;

import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.rancour.clan.models.VerificationStatus;
import com.rancour.clan.services.VerificationService;

final class VerificationPanel extends JPanel
{
	VerificationPanel(VerificationService service)
	{
		super(new BorderLayout());
		JPanel page = UiComponents.page();
		JPanel content = new JPanel();
		content.setOpaque(false);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		VerificationStatus status = service.getCurrentStatus();
		JLabel statusLabel = new JLabel("Status: " + status.getMessage());
		JButton linkButton = new JButton("Link Account");
		linkButton.addActionListener(event ->
		{
			VerificationStatus result = service.requestAccountLink();
			statusLabel.setText("Status: " + result.getMessage());
		});

		content.add(UiComponents.heading("Verification"));
		content.add(UiComponents.card("Clan account", "Connect your RuneLite identity to your Rancour clan profile.", "Phase 1 mock"));
		content.add(Box.createVerticalStrut(8));
		content.add(statusLabel);
		content.add(Box.createVerticalStrut(8));
		content.add(linkButton);
		page.add(content, BorderLayout.NORTH);
		add(page, BorderLayout.CENTER);
	}
}
