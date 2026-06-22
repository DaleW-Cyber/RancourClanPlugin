package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.runelite.client.ui.ColorScheme;
import com.rancour.clan.api.ApiException;

final class UiComponents
{
	private static final int TEXT_WIDTH = 138;

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
		JLabel label = new JLabel();
		setWrappedText(label, text);
		label.setForeground(Color.LIGHT_GRAY);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	static JLabel statusLabel(String text)
	{
		JLabel label = new JLabel()
		{
			@Override
			public void setText(String value)
			{
				super.setText("<html><body style='width:" + TEXT_WIDTH + "px'>" + html(UiComponents.value(value)) + "</body></html>");
			}
		};
		label.setForeground(Color.LIGHT_GRAY);
		label.setText(text);
		return label;
	}

	static void setWrappedText(JLabel label, String text)
	{
		label.setText("<html><body style='width:" + TEXT_WIDTH + "px'>" + html(value(text)) + "</body></html>");
	}

	static JPanel card(String title, String body, String footer)
	{
		JPanel card = card(title);
		if (!value(body).isEmpty())
		{
			card.add(wrapped(body));
		}
		if (!value(footer).isEmpty())
		{
			JLabel footerLabel = wrapped(footer);
			footerLabel.setForeground(Color.GRAY);
			card.add(Box.createVerticalStrut(4));
			card.add(footerLabel);
		}
		return card;
	}

	static JPanel detailsCard(String title, String body, String... fields)
	{
		JPanel card = card(title);
		if (!value(body).isEmpty())
		{
			card.add(wrapped(body));
			card.add(Box.createVerticalStrut(6));
		}
		for (int index = 0; index + 1 < fields.length; index += 2)
		{
			card.add(fieldRow(fields[index], fields[index + 1]));
		}
		return card;
	}

	static JPanel fieldRow(String label, String fieldValue)
	{
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setOpaque(false);
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		JLabel name = new JLabel("<html><b>" + html(label) + "</b></html>");
		name.setForeground(Color.GRAY);
		name.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.add(name);
		row.add(wrapped(fieldValue));
		row.add(Box.createVerticalStrut(5));
		return row;
	}

	private static JPanel card(String title)
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 8, 0),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		JLabel titleLabel = new JLabel("<html><body style='width:" + TEXT_WIDTH + "px'><b>" + html(value(title)) + "</b></body></html>");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.add(titleLabel);
		card.add(Box.createVerticalStrut(5));
		return card;
	}

	static JPanel contentPanel()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content.setAlignmentX(Component.LEFT_ALIGNMENT);
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

	static boolean isApiStatus(Throwable error, int statusCode)
	{
		Throwable current = error;
		while (current != null)
		{
			if (current instanceof ApiException && ((ApiException) current).getStatusCode() == statusCode)
			{
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	static String value(String value)
	{
		return value == null ? "" : value;
	}

	private static String html(String value)
	{
		return value(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			.replace("\r\n", "<br>").replace("\n", "<br>");
	}
}
