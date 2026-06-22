package com.rancour.clan.ui;

import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.rancour.clan.models.DropSubmission;
import com.rancour.clan.services.DropService;

final class DropsPanel extends JPanel
{
	DropsPanel(DropService service)
	{
		super(new BorderLayout());
		JPanel page = UiComponents.page();
		JPanel content = new JPanel();
		content.setOpaque(false);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JTextField itemField = new JTextField("Example drop");
		JTextField sourceField = new JTextField("Example boss");
		JLabel resultLabel = new JLabel("No drop submitted");
		JButton submitButton = new JButton("Submit Drop (Mock)");
		submitButton.addActionListener(event -> resultLabel.setText(
			service.submit(new DropSubmission(itemField.getText(), sourceField.getText()))
		));

		content.add(UiComponents.heading("Drops"));
		content.add(UiComponents.card("Drop submission", "This form is ready to connect to detection and logging services.", "No data leaves RuneLite"));
		content.add(Box.createVerticalStrut(8));
		content.add(new JLabel("Item"));
		content.add(itemField);
		content.add(Box.createVerticalStrut(5));
		content.add(new JLabel("Boss or activity"));
		content.add(sourceField);
		content.add(Box.createVerticalStrut(8));
		content.add(submitButton);
		content.add(Box.createVerticalStrut(8));
		content.add(resultLabel);
		page.add(content, BorderLayout.NORTH);
		add(page, BorderLayout.CENTER);
	}
}
