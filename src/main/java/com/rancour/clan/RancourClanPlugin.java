package com.rancour.clan;

import com.google.inject.Provides;
import com.google.gson.Gson;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.time.Duration;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import okhttp3.OkHttpClient;
import com.rancour.clan.api.ClanApiClient;
import com.rancour.clan.api.RestClanApiClient;
import com.rancour.clan.config.RancourClanConfig;
import com.rancour.clan.models.DropCandidate;
import com.rancour.clan.services.AnnouncementService;
import com.rancour.clan.services.AnnouncementNotifier;
import com.rancour.clan.services.ApiServices;
import com.rancour.clan.services.DropService;
import com.rancour.clan.services.DropDetector;
import com.rancour.clan.services.DuplicateDropGuard;
import com.rancour.clan.services.EventService;
import com.rancour.clan.services.NotifyingAnnouncementService;
import com.rancour.clan.services.PluginSettingsService;
import com.rancour.clan.services.RuneLiteSessionStore;
import com.rancour.clan.services.RuneLiteSeenAnnouncementStore;
import com.rancour.clan.services.SessionStore;
import com.rancour.clan.services.StaffService;
import com.rancour.clan.services.TeamService;
import com.rancour.clan.services.VerificationService;
import com.rancour.clan.ui.RancourClanPanel;

@PluginDescriptor(
	name = "Rancour Clan",
	description = "Rancour clan verification, announcements, events, teams, and drop tools",
	tags = {"clan", "pvm", "events", "drops", "verification", "teams"}
)
public class RancourClanPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private RancourClanConfig config;

	@Inject
	private VerificationService verificationService;

	@Inject
	private AnnouncementService announcementService;

	@Inject
	private EventService eventService;

	@Inject
	private DropService dropService;

	@Inject
	private TeamService teamService;

	@Inject
	private StaffService staffService;

	@Inject
	private PluginSettingsService settingsService;

	private NavigationButton navigationButton;
	private volatile RancourClanPanel panel;
	private volatile String activeRsn = "";
	private Timer autoRefreshTimer;
	private final DropDetector dropDetector = new DropDetector();
	private final DuplicateDropGuard duplicateDropGuard = new DuplicateDropGuard(Duration.ofSeconds(30));

	@Override
	protected void startUp()
	{
		activeRsn = currentAccountName();
		SwingUtilities.invokeLater(() ->
		{
			panel = new RancourClanPanel(
				verificationService,
				announcementService,
				eventService,
				dropService,
				teamService,
				staffService,
				settingsService,
				() -> activeRsn
			);
			navigationButton = NavigationButton.builder()
				.tooltip("Rancour Clan")
				.icon(createNavigationIcon())
				.priority(5)
				.panel(panel)
				.build();
			clientToolbar.addNavigation(navigationButton);
			startAutoRefresh();
		});
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		activeRsn = currentAccountName();
	}

	@Override
	protected void shutDown()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (navigationButton != null)
			{
				clientToolbar.removeNavigation(navigationButton);
				navigationButton = null;
			}
			if (autoRefreshTimer != null)
			{
				autoRefreshTimer.stop();
				autoRefreshTimer = null;
			}
			panel = null;
		});
	}

	private void startAutoRefresh()
	{
		if (!config.automaticRefresh())
		{
			return;
		}
		int seconds = Math.max(30, config.refreshIntervalSeconds());
		autoRefreshTimer = new Timer(seconds * 1000, event ->
		{
			RancourClanPanel currentPanel = panel;
			if (currentPanel != null)
			{
				currentPanel.refreshAll();
			}
		});
		autoRefreshTimer.setRepeats(true);
		autoRefreshTimer.start();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		String rsn = currentAccountName();
		activeRsn = rsn;
		if (rsn.isEmpty()) { rsn = "Unknown"; }
		dropDetector.fromChatMessage(event.getMessage(), rsn).filter(duplicateDropGuard::accept).ifPresent(this::offerDropCandidate);
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived event)
	{
		String rsn = currentAccountName();
		activeRsn = rsn;
		if (rsn.isEmpty()) { rsn = "Unknown"; }
		String source = event.getNpc() == null ? "Unknown NPC" : event.getNpc().getName();
		for (ItemStack item : event.getItems())
		{
			long totalValue = (long) itemManager.getItemPrice(item.getId()) * item.getQuantity();
			if (totalValue >= config.minimumDropValue())
			{
				String itemName = itemManager.getItemComposition(item.getId()).getName();
				DropCandidate candidate = dropDetector.fromNpcLoot(itemName, source, rsn);
				if (duplicateDropGuard.accept(candidate))
				{
					offerDropCandidate(candidate);
				}
			}
		}
	}

	private void offerDropCandidate(DropCandidate candidate)
	{
		RancourClanPanel currentPanel = panel;
		if (currentPanel != null && currentPanel.acceptsDropCandidate(candidate))
		{
			currentPanel.offerDropCandidate(candidate);
		}
	}

	private String currentAccountName()
	{
		return client.getLocalPlayer() == null || client.getLocalPlayer().getName() == null
			? ""
			: client.getLocalPlayer().getName().trim();
	}

	@Provides
	RancourClanConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RancourClanConfig.class);
	}

	@Provides
	@Singleton
	SessionStore provideSessionStore(ConfigManager configManager)
	{
		return new RuneLiteSessionStore(configManager);
	}

	@Provides
	@Singleton
	ClanApiClient provideApiClient(OkHttpClient httpClient, Gson gson)
	{
		return new RestClanApiClient(httpClient, gson, RestClanApiClient.defaultBaseUrl());
	}

	@Provides
	@Singleton
	VerificationService provideVerificationService(ClanApiClient api, SessionStore sessions)
	{
		return ApiServices.verification(api, sessions);
	}

	@Provides
	AnnouncementService provideAnnouncementService(ClanApiClient api, VerificationService verification,
		RancourClanConfig config, ConfigManager configManager, ClientThread clientThread, Client client)
	{
		AnnouncementService service = ApiServices.announcements(api, verification);
		AnnouncementNotifier notifier = new AnnouncementNotifier(
			new RuneLiteSeenAnnouncementStore(configManager),
			message -> clientThread.invokeLater(() -> client.addChatMessage(
				ChatMessageType.GAMEMESSAGE, "", message, null))
		);
		return new NotifyingAnnouncementService(service, notifier, config);
	}

	@Provides
	EventService provideEventService(ClanApiClient api, VerificationService verification)
	{
		return ApiServices.events(api, verification);
	}

	@Provides
	DropService provideDropService(ClanApiClient api, VerificationService verification)
	{
		return ApiServices.drops(api, verification);
	}

	@Provides
	TeamService provideTeamService(ClanApiClient api, VerificationService verification)
	{
		return ApiServices.teams(api, verification, () -> activeRsn);
	}

	@Provides
	StaffService provideStaffService(ClanApiClient api, VerificationService verification)
	{
		return ApiServices.staff(api, verification);
	}

	@Provides
	PluginSettingsService providePluginSettingsService(ClanApiClient api)
	{
		return ApiServices.settings(api);
	}

	private static BufferedImage createNavigationIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(184, 32, 37));
		graphics.fillRoundRect(1, 1, 14, 14, 4, 4);
		graphics.setColor(Color.WHITE);
		graphics.drawString("R", 4, 12);
		graphics.dispose();
		return image;
	}
}
