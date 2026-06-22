package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.runelite.client.ui.PluginPanel;
import com.rancour.clan.services.AnnouncementService;
import com.rancour.clan.services.DropService;
import com.rancour.clan.services.EventService;
import com.rancour.clan.services.VerificationService;

public final class RancourClanPanel extends PluginPanel
{
	private static final String VERIFICATION = "verification";
	private static final String ANNOUNCEMENTS = "announcements";
	private static final String EVENTS = "events";
	private static final String DROPS = "drops";

	public RancourClanPanel(
		VerificationService verificationService,
		AnnouncementService announcementService,
		EventService eventService,
		DropService dropService)
	{
		super(false);
		setLayout(new BorderLayout());

		CardLayout cardLayout = new CardLayout();
		JPanel cards = new JPanel(cardLayout);
		cards.add(new VerificationPanel(verificationService), VERIFICATION);
		cards.add(new AnnouncementsPanel(announcementService), ANNOUNCEMENTS);
		cards.add(new EventsPanel(eventService), EVENTS);
		cards.add(new DropsPanel(dropService), DROPS);

		JPanel navigation = new JPanel(new GridLayout(2, 2, 4, 4));
		navigation.add(button("Verify", cardLayout, cards, VERIFICATION));
		navigation.add(button("News", cardLayout, cards, ANNOUNCEMENTS));
		navigation.add(button("Events", cardLayout, cards, EVENTS));
		navigation.add(button("Drops", cardLayout, cards, DROPS));

		add(navigation, BorderLayout.NORTH);
		add(cards, BorderLayout.CENTER);
	}

	private static JButton button(String label, CardLayout layout, JPanel cards, String page)
	{
		JButton button = new JButton(label);
		button.addActionListener(event -> layout.show(cards, page));
		return button;
	}
}
