package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.runelite.client.ui.ColorScheme;

final class UiComponents
{
	private UiComponents() { }

	static JLabel heading(String text)
	{
		JLabel label = new JLabel("<html><h2>" + html(text) + "</h2></html>");
		label.setForeground(Color.WHITE);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		return label;
	}

	static JLabel wrapped(String text)
	{
		JLabel label = new JLabel("<html><body style='width:190px'>" + html(value(text)) + "</body></html>");
		label.setForeground(Color.LIGHT_GRAY);
		return label;
	}

	static JPanel card(String title, String body, String footer)
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 8, 0),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		JLabel titleLabel = new JLabel("<html><b>" + html(value(title)) + "</b></html>");
		titleLabel.setForeground(Color.WHITE);
		card.add(titleLabel);
		card.add(wrapped(body));
		JLabel footerLabel = new JLabel(value(footer));
		footerLabel.setForeground(Color.GRAY);
		card.add(footerLabel);
		return card;
	}

	static JPanel contentPanel()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return content;
	}

	static JScrollPane scroll(JPanel content)
	{
		JScrollPane scroll = new JScrollPane(content,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		return scroll;
	}

	static JPanel page(JPanel controls, JPanel content)
	{
		JPanel page = new JPanel(new BorderLayout());
		page.setBackground(ColorScheme.DARK_GRAY_COLOR);
		if (controls != null)
		{
			page.add(controls, BorderLayout.NORTH);
		}
		page.add(scroll(content), BorderLayout.CENTER);
		return page;
	}

	static String errorMessage(Throwable error)
	{
		Throwable current = error;
		while (current.getCause() != null)
		{
			current = current.getCause();
		}
		return value(current.getMessage()).isEmpty() ? "Unexpected error" : current.getMessage();
	}

	static String value(String value)
	{
		return value == null ? "" : value;
	}

	private static String html(String value)
	{
		return value(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
