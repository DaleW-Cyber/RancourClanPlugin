package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.ColorScheme;

final class UiComponents
{
	private UiComponents()
	{
	}

	static JLabel heading(String text)
	{
		JLabel label = new JLabel("<html><h2>" + text + "</h2></html>");
		label.setForeground(Color.WHITE);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		return label;
	}

	static JPanel card(String title, String body, String footer)
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 8, 0),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)
		));

		JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
		titleLabel.setForeground(Color.WHITE);
		JLabel bodyLabel = new JLabel("<html><body style='width:190px'>" + body + "</body></html>");
		bodyLabel.setForeground(Color.LIGHT_GRAY);
		JLabel footerLabel = new JLabel(footer);
		footerLabel.setForeground(Color.GRAY);

		card.add(titleLabel);
		card.add(bodyLabel);
		card.add(footerLabel);
		return card;
	}

	static JPanel page()
	{
		JPanel page = new JPanel(new BorderLayout());
		page.setBackground(ColorScheme.DARK_GRAY_COLOR);
		page.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return page;
	}
}
