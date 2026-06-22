package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.function.Supplier;
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
	private final VerificationPanel verificationPanel;
	private final AnnouncementsPanel announcementsPanel;
	private final EventsPanel eventsPanel;
	private final TeamsPanel teamsPanel;
	private final JButton staffButton;
	private final StaffPanel staffPanel;
	private final CardLayout cardLayout;
	private final JPanel cards;

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		boolean mockMode)
	{
		this(verificationService, announcementService, eventService, dropService, teamService,
			staffService, mockMode, () -> "");
	}

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		boolean mockMode, Supplier<String> activeRsn)
	{
		super(false);
		setLayout(new BorderLayout());
		cardLayout = new CardLayout();
		cards = new JPanel(cardLayout);
		verificationPanel = new VerificationPanel(verificationService, activeRsn);
		announcementsPanel = new AnnouncementsPanel(announcementService);
		eventsPanel = new EventsPanel(eventService);
		teamsPanel = new TeamsPanel(teamService);
		dropsPanel = new DropsPanel(dropService, activeRsn);
		staffPanel = new StaffPanel(staffService, announcementsPanel::refresh);
		cards.add(verificationPanel, "verification");
		cards.add(announcementsPanel, "announcements");
		cards.add(eventsPanel, "events");
		cards.add(dropsPanel, "drops");
		cards.add(teamsPanel, "teams");
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

	public void refreshAll()
	{
		verificationPanel.refresh();
		announcementsPanel.refresh();
		eventsPanel.refresh();
		teamsPanel.refresh();
		if (staffButton.isVisible())
		{
			staffPanel.refreshPending();
		}
	}

	private void updateStaffAccess(MemberProfile profile)
	{
		dropsPanel.setProfile(profile);
		boolean staff = profile != null && profile.isStaff();
		staffButton.setVisible(staff);
		staffButton.getParent().revalidate();
		if (staff)
		{
			staffPanel.refreshPending();
		}
		else
		{
			cardLayout.show(cards, "verification");
		}
	}

	boolean isStaffButtonVisible()
	{
		return staffButton.isVisible();
	}

	private static JButton button(String label, CardLayout layout, JPanel cards, String page)
	{
		JButton button = new JButton(label);
		button.addActionListener(event -> layout.show(cards, page));
		return button;
	}
}
