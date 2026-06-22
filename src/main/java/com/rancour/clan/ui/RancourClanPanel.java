package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;
import com.rancour.clan.models.DropCandidate;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.services.AnnouncementService;
import com.rancour.clan.services.DropService;
import com.rancour.clan.services.EventService;
import com.rancour.clan.services.StaffService;
import com.rancour.clan.services.TeamService;
import com.rancour.clan.services.VerificationService;

public final class RancourClanPanel extends PluginPanel
{
	private final DropsPanel dropsPanel;
	private final JButton staffButton;
	private final StaffPanel staffPanel;

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		boolean mockMode)
	{
		super(false);
		setLayout(new BorderLayout());
		CardLayout cardLayout = new CardLayout();
		JPanel cards = new JPanel(cardLayout);
		VerificationPanel verificationPanel = new VerificationPanel(verificationService);
		dropsPanel = new DropsPanel(dropService);
		staffPanel = new StaffPanel(staffService);
		cards.add(verificationPanel, "verification");
		cards.add(new AnnouncementsPanel(announcementService), "announcements");
		cards.add(new EventsPanel(eventService), "events");
		cards.add(dropsPanel, "drops");
		cards.add(new TeamsPanel(teamService), "teams");
		cards.add(staffPanel, "staff");

		JPanel navigation = new JPanel(new GridLayout(3, 2, 4, 4));
		navigation.add(button("Verify", cardLayout, cards, "verification"));
		navigation.add(button("News", cardLayout, cards, "announcements"));
		navigation.add(button("Events", cardLayout, cards, "events"));
		navigation.add(button("Drops", cardLayout, cards, "drops"));
		navigation.add(button("Teams", cardLayout, cards, "teams"));
		staffButton = button("Staff", cardLayout, cards, "staff");
		staffButton.setVisible(false);
		navigation.add(staffButton);

		JPanel top = new JPanel(new BorderLayout());
		if (mockMode)
		{
			top.add(new JLabel("MOCK MODE - local data only"), BorderLayout.NORTH);
		}
		top.add(navigation, BorderLayout.CENTER);
		add(top, BorderLayout.NORTH);
		add(cards, BorderLayout.CENTER);
		verificationService.addProfileListener(profile -> SwingUtilities.invokeLater(() -> updateStaffAccess(profile)));
		verificationPanel.refresh();
	}

	public void offerDropCandidate(DropCandidate candidate)
	{
		SwingUtilities.invokeLater(() -> dropsPanel.offerCandidate(candidate));
	}

	private void updateStaffAccess(MemberProfile profile)
	{
		boolean staff = profile != null && profile.isStaff();
		staffButton.setVisible(staff);
		staffButton.getParent().revalidate();
		if (staff)
		{
			staffPanel.refreshPending();
		}
	}

	private static JButton button(String label, CardLayout layout, JPanel cards, String page)
	{
		JButton button = new JButton(label);
		button.addActionListener(event -> layout.show(cards, page));
		return button;
	}
}
