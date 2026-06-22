package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;
import com.rancour.clan.models.DropCandidate;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.PluginSettings;
import com.rancour.clan.services.AnnouncementService;
import com.rancour.clan.services.DropService;
import com.rancour.clan.services.EventService;
import com.rancour.clan.services.PluginSettingsService;
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
	private final JButton dropsButton;
	private final JButton staffButton;
	private final StaffPanel staffPanel;
	private final PluginSettingsService settingsService;
	private final CardLayout cardLayout;
	private final JPanel cards;
	private volatile boolean dropsPanelEnabled = true;
	private volatile Set<String> approvedDropKeys = Collections.emptySet();

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		boolean mockMode)
	{
		this(verificationService, announcementService, eventService, dropService, teamService,
			staffService, () -> java.util.concurrent.CompletableFuture.completedFuture(new PluginSettings(true, Collections.emptyList())),
			mockMode, () -> "");
	}

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		PluginSettingsService settingsService, boolean mockMode)
	{
		this(verificationService, announcementService, eventService, dropService, teamService,
			staffService, settingsService, mockMode, () -> "");
	}

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		PluginSettingsService settingsService, boolean mockMode, Supplier<String> activeRsn)
	{
		super(false);
		this.settingsService = settingsService;
		setLayout(new BorderLayout());
		cardLayout = new CardLayout();
		cards = new JPanel(cardLayout);
		verificationPanel = new VerificationPanel(verificationService, activeRsn);
		announcementsPanel = new AnnouncementsPanel(announcementService);
		eventsPanel = new EventsPanel(eventService);
		teamsPanel = new TeamsPanel(teamService);
		dropsPanel = new DropsPanel(dropService, activeRsn);
		staffPanel = new StaffPanel(staffService, () ->
		{
			announcementsPanel.refresh();
			refreshSettings();
		});
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
		dropsButton = button("Drops", cardLayout, cards, "drops");
		navigation.add(dropsButton);
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
		refreshSettings();
	}

	public boolean acceptsDropCandidate(DropCandidate candidate)
	{
		return dropsPanelEnabled && approvedDropKeys.contains(normalizeDrop(candidate.getItemName()));
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
		refreshSettings();
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

	private void refreshSettings()
	{
		settingsService.loadSettings().whenComplete((settings, error) -> SwingUtilities.invokeLater(() ->
		{
			if (error != null || settings == null)
			{
				return;
			}
			dropsPanelEnabled = settings.isDropsPanelEnabled();
			Set<String> keys = new HashSet<>();
			for (String drop : settings.getApprovedDrops())
			{
				keys.add(normalizeDrop(drop));
			}
			approvedDropKeys = keys;
			dropsButton.setVisible(dropsPanelEnabled);
			dropsPanel.setDropsPanelEnabled(dropsPanelEnabled);
			staffPanel.applySettings(settings);
			dropsButton.getParent().revalidate();
			if (!dropsPanelEnabled)
			{
				dropsPanel.showDisabled();
			}
		}));
	}

	private static String normalizeDrop(String value)
	{
		return UiComponents.value(value).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
	}

	boolean isStaffButtonVisible()
	{
		return staffButton.isVisible();
	}

	boolean isDropsButtonVisible()
	{
		return dropsButton.isVisible();
	}

	private static JButton button(String label, CardLayout layout, JPanel cards, String page)
	{
		JButton button = new JButton(label);
		button.addActionListener(event -> layout.show(cards, page));
		return button;
	}
}
