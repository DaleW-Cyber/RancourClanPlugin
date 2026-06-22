package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;
import net.runelite.client.ui.ColorScheme;
import com.rancour.clan.api.ApiException;

final class UiComponents
{
	private static final int TEXT_WIDTH = 172;
	private static final DateTimeFormatter SHORT_DATE =
		DateTimeFormatter.ofPattern("dd MMM HH:mm").withZone(ZoneId.systemDefault());

	private UiComponents() { }

	static JLabel heading(String text)
	{
		JLabel label = new JLabel("<html><b>" + html(text) + "</b></html>");
		label.setForeground(RancourTheme.TEXT);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		return label;
	}

	static JTextArea wrapped(String text)
	{
		return new WrappingTextArea(text, false);
	}

	static JTextArea statusLabel(String text)
	{
		JTextArea label = new WrappingTextArea(text, false);
		label.setForeground(RancourTheme.MUTED);
		return label;
	}

	static JPanel card(String title, String body, String footer)
	{
		return card(title, body, footer, null);
	}

	static JPanel card(String title, String body, String footer, Color accent)
	{
		JPanel card = card(title);
		applyAccent(card, accent);
		if (!value(body).isEmpty())
		{
			card.add(wrapped(body));
		}
		if (!value(footer).isEmpty())
		{
			JTextArea footerLabel = wrapped(footer);
			footerLabel.setForeground(RancourTheme.MUTED);
			card.add(Box.createVerticalStrut(4));
			card.add(footerLabel);
		}
		return card;
	}

	static JPanel detailsCard(String title, String body, String... fields)
	{
		return detailsCard(title, body, null, fields);
	}

	static JPanel detailsCard(String title, String body, Color accent, String... fields)
	{
		JPanel card = card(title);
		applyAccent(card, accent);
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
		JLabel name = new JLabel("<html><b>" + html(label) + "</b></html>");
		name.setForeground(RancourTheme.MUTED);
		name.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.add(name);
		row.add(wrapped(fieldValue));
		row.add(Box.createVerticalStrut(3));
		limitHeight(row);
		return row;
	}

	private static JPanel card(String title)
	{
		JPanel card = new CompactPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(RancourTheme.CARD_BACKGROUND);
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		JTextArea titleLabel = new WrappingTextArea(title, true);
		titleLabel.setForeground(RancourTheme.TEXT);
		card.add(titleLabel);
		JSeparator separator = new JSeparator();
		separator.setForeground(RancourTheme.CARD_SEPARATOR);
		separator.setBackground(RancourTheme.CARD_SEPARATOR);
		separator.setAlignmentX(Component.LEFT_ALIGNMENT);
		separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		card.add(separator);
		card.add(Box.createVerticalStrut(3));
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 7, 0),
			BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(RancourTheme.CARD_BORDER),
				BorderFactory.createEmptyBorder(5, 6, 5, 6))));
		return card;
	}

	static JTextArea badge(String text, Color color)
	{
		JTextArea badge = new WrappingTextArea(text, true);
		badge.setForeground(color);
		return badge;
	}

	static JButton successButton(String text)
	{
		JButton button = compact(new JButton(text));
		styleButton(button, RancourTheme.SUCCESS);
		return button;
	}

	static JButton dangerButton(String text)
	{
		JButton button = compact(new JButton(text));
		styleButton(button, RancourTheme.DANGER);
		return button;
	}

	static JButton warningButton(String text)
	{
		JButton button = compact(new JButton(text));
		styleButton(button, RancourTheme.WARNING);
		return button;
	}

	static JButton neutralButton(String text)
	{
		JButton button = compact(new JButton(text));
		styleButton(button, RancourTheme.CARD_BORDER);
		return button;
	}

	private static void styleButton(JButton button, Color color)
	{
		button.setForeground(RancourTheme.TEXT);
		button.setBackground(color.darker());
		button.setOpaque(true);
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(color),
			BorderFactory.createEmptyBorder(3, 5, 3, 5)));
	}

	private static void applyAccent(JPanel card, Color accent)
	{
		if (accent == null)
		{
			return;
		}
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 7, 0),
			BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
				BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(RancourTheme.CARD_BORDER),
					BorderFactory.createEmptyBorder(5, 6, 5, 6)))));
	}

	static <T extends JComponent> T compact(T component)
	{
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		limitHeight(component);
		return component;
	}

	private static void limitHeight(JComponent component)
	{
		Dimension preferred = component.getPreferredSize();
		component.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
	}

	static JPanel contentPanel()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		content.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
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

	static String shortDate(String value)
	{
		if (value(value).isEmpty())
		{
			return "Not set";
		}
		try
		{
			return SHORT_DATE.format(Instant.parse(value));
		}
		catch (DateTimeParseException ignored)
		{
			return value;
		}
	}

	static String nowShort()
	{
		return SHORT_DATE.format(Instant.now());
	}

	static JTextArea small(String text)
	{
		JTextArea label = wrapped(text);
		label.setForeground(RancourTheme.MUTED);
		label.setFont(label.getFont().deriveFont(10f));
		return label;
	}

	private static String html(String value)
	{
		return value(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			.replace("\r\n", "<br>").replace("\n", "<br>");
	}

	private static final class WrappingTextArea extends JTextArea
	{
		private WrappingTextArea(String text, boolean bold)
		{
			super(value(text));
			setEditable(false);
			setFocusable(false);
			setOpaque(false);
			setLineWrap(true);
			setWrapStyleWord(true);
			setBorder(null);
			setFont(getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN));
			setForeground(RancourTheme.TEXT);
			setAlignmentX(Component.LEFT_ALIGNMENT);
			setSize(new Dimension(TEXT_WIDTH, Short.MAX_VALUE));
		}

		@Override
		public Dimension getPreferredSize()
		{
			setSize(new Dimension(TEXT_WIDTH, Short.MAX_VALUE));
			Dimension preferred = super.getPreferredSize();
			return new Dimension(TEXT_WIDTH, preferred.height);
		}

		@Override
		public Dimension getMaximumSize()
		{
			return getPreferredSize();
		}
	}

	private static final class CompactPanel extends JPanel
	{
		@Override
		public Dimension getMaximumSize()
		{
			Dimension preferred = getPreferredSize();
			return new Dimension(Integer.MAX_VALUE, preferred.height);
		}
	}
}
