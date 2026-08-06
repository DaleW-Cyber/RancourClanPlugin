package com.rancour.clan.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
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
	private static final int AUTO_REFRESH_JITTER_MAX_MS = 20_000;

	private final DropsPanel dropsPanel;
	private final VerificationPanel verificationPanel;
	private final AnnouncementsPanel announcementsPanel;
	private final EventsPanel eventsPanel;
	private final TeamsPanel teamsPanel;
	private final JButton dropsButton;
	private final JButton staffButton;
	private final StaffPanel staffPanel;
	private final PluginSettingsService settingsService;
	private final VerificationService verificationService;
	private final CardLayout cardLayout;
	private final JPanel cards;
	private volatile String currentPage = "verification";
	private volatile boolean userSelectedPage;
	private volatile boolean lastVerifiedWithSession;
	private volatile boolean dropsPanelEnabled = true;
	private volatile boolean dropsVisible;
	private volatile boolean dropsCanSubmit;
	private volatile Set<String> approvedDropKeys = Collections.emptySet();
	private boolean automaticRefreshScheduled;

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		boolean ignoredMockMode)
	{
		this(verificationService, announcementService, eventService, dropService, teamService, staffService);
	}

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService)
	{
		this(verificationService, announcementService, eventService, dropService, teamService,
			staffService, () -> java.util.concurrent.CompletableFuture.completedFuture(new PluginSettings(true, Collections.emptyList())),
			() -> "");
	}

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		PluginSettingsService settingsService, boolean ignoredMockMode)
	{
		this(verificationService, announcementService, eventService, dropService, teamService,
			staffService, settingsService, () -> "");
	}

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		PluginSettingsService settingsService)
	{
		this(verificationService, announcementService, eventService, dropService, teamService,
			staffService, settingsService, () -> "");
	}

	public RancourClanPanel(VerificationService verificationService, AnnouncementService announcementService,
		EventService eventService, DropService dropService, TeamService teamService, StaffService staffService,
		PluginSettingsService settingsService, Supplier<String> activeRsn)
	{
		super(false);
		this.verificationService = verificationService;
		this.settingsService = settingsService;
		setLayout(new BorderLayout());
		cardLayout = new CardLayout();
		cards = new JPanel(cardLayout);
		verificationPanel = new VerificationPanel(verificationService, activeRsn);
		announcementsPanel = new AnnouncementsPanel(announcementService);
		eventsPanel = new EventsPanel(eventService);
		teamsPanel = new TeamsPanel(teamService, activeRsn);
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
		navigation.add(button("Verify", "verification"));
		navigation.add(button("News", "announcements"));
		navigation.add(button("Events", "events"));
		dropsButton = button("Drops", "drops");
		dropsButton.setVisible(false);
		navigation.add(dropsButton);
		navigation.add(button("Teams", "teams"));
		staffButton = button("Staff", "staff");
		staffButton.setVisible(false);
		navigation.add(staffButton);

		JPanel top = new JPanel(new BorderLayout());
		top.add(navigation, BorderLayout.CENTER);
		add(top, BorderLayout.NORTH);
		add(cards, BorderLayout.CENTER);
		verificationService.addProfileListener(profile -> SwingUtilities.invokeLater(() -> updateStaffAccess(profile)));
		lastVerifiedWithSession = verifiedWithSession(verificationService.getCurrentProfile());
		showPage(lastVerifiedWithSession ? "announcements" : "verification", false);
		verificationPanel.refresh();
		if (lastVerifiedWithSession)
		{
			refreshVerifiedData();
		}
	}

	public boolean acceptsDropCandidate(DropCandidate candidate)
	{
		return dropsVisible && dropsCanSubmit && approvedDropKeys.contains(normalizeDrop(candidate.getItemName()));
	}

	public void offerDropCandidate(DropCandidate candidate)
	{
		SwingUtilities.invokeLater(() -> dropsPanel.offerCandidate(candidate));
	}

	public void refreshAll()
	{
		if (automaticRefreshScheduled)
		{
			return;
		}
		automaticRefreshScheduled = true;
		int delay = ThreadLocalRandom.current().nextInt(AUTO_REFRESH_JITTER_MAX_MS + 1);
		Timer timer = new Timer(delay, event ->
		{
			((Timer) event.getSource()).stop();
			automaticRefreshScheduled = false;
			performAutomaticRefresh();
		});
		timer.setRepeats(false);
		timer.start();
	}

	private void performAutomaticRefresh()
	{
		verificationPanel.refresh();
		if (!lastVerifiedWithSession || verificationPanel.hasRefreshFailure())
		{
			return;
		}
		refreshVerifiedData();
	}

	private void refreshVerifiedData()
	{
		announcementsPanel.refresh();
		eventsPanel.refresh();
		teamsPanel.refreshIfIdle();
		refreshSettings();
		if (staffButton.isVisible())
		{
			staffPanel.refreshPending();
		}
	}

	private void updateStaffAccess(MemberProfile profile)
	{
		dropsPanel.setProfile(profile);
		boolean verifiedWithSession = verifiedWithSession(profile);
		boolean becameVerified = verifiedWithSession && !lastVerifiedWithSession;
		boolean lostVerification = !verifiedWithSession && lastVerifiedWithSession;
		lastVerifiedWithSession = verifiedWithSession;
		boolean staff = profile != null && profile.isStaff() && verifiedWithSession;
		staffButton.setVisible(staff);
		staffButton.getParent().revalidate();
		if (staff && !becameVerified)
		{
			staffPanel.refreshPending();
		}
		if (becameVerified)
		{
			showPage("announcements", false);
			userSelectedPage = false;
			refreshVerifiedData();
		}
		else if (lostVerification)
		{
			showPage("verification", false);
			userSelectedPage = false;
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
			dropsVisible = settings.isDropsVisible();
			dropsCanSubmit = settings.canSubmitDrops();
			Set<String> keys = new HashSet<>();
			for (String drop : settings.getApprovedDrops())
			{
				keys.add(normalizeDrop(drop));
			}
			approvedDropKeys = keys;
			dropsButton.setVisible(dropsVisible);
			dropsPanel.applySettings(settings);
			staffPanel.applySettings(settings);
			dropsButton.getParent().revalidate();
			if (!dropsVisible)
			{
				dropsPanel.showDisabled();
				if ("drops".equals(currentPage))
				{
					showPage(lastVerifiedWithSession ? "announcements" : "verification", false);
				}
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

	private boolean verifiedWithSession(MemberProfile profile)
	{
		return profile != null && !UiComponents.value(verificationService.getSessionToken()).trim().isEmpty();
	}

	private void showPage(String page, boolean manual)
	{
		currentPage = page;
		if (manual)
		{
			userSelectedPage = true;
		}
		cardLayout.show(cards, page);
	}

	String currentPage()
	{
		return currentPage;
	}

	boolean hasUserSelectedPage()
	{
		return userSelectedPage;
	}

	private JButton button(String label, String page)
	{
		JButton button = new JButton(label);
		button.addActionListener(event -> showPage(page, true));
		return button;
	}
}
