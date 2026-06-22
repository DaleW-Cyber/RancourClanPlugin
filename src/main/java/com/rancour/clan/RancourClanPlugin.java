package com.rancour.clan;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import com.rancour.clan.api.ClanApiClient;
import com.rancour.clan.api.RestClanApiClient;
import com.rancour.clan.config.RancourClanConfig;
import com.rancour.clan.services.AnnouncementService;
import com.rancour.clan.services.DropService;
import com.rancour.clan.services.EventService;
import com.rancour.clan.services.PlaceholderAnnouncementService;
import com.rancour.clan.services.PlaceholderDropService;
import com.rancour.clan.services.PlaceholderEventService;
import com.rancour.clan.services.PlaceholderVerificationService;
import com.rancour.clan.services.VerificationService;
import com.rancour.clan.ui.RancourClanPanel;

@PluginDescriptor(
	name = "Rancour Clan",
	description = "Rancour clan verification, announcements, events, and drop tools",
	tags = {"clan", "pvm", "events", "drops", "verification"}
)
public class RancourClanPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private VerificationService verificationService;

	@Inject
	private AnnouncementService announcementService;

	@Inject
	private EventService eventService;

	@Inject
	private DropService dropService;

	private NavigationButton navigationButton;

	@Override
	protected void startUp()
	{
		SwingUtilities.invokeLater(() ->
		{
			RancourClanPanel panel = new RancourClanPanel(
				verificationService,
				announcementService,
				eventService,
				dropService
			);
			navigationButton = NavigationButton.builder()
				.tooltip("Rancour Clan")
				.icon(createNavigationIcon())
				.priority(5)
				.panel(panel)
				.build();
			clientToolbar.addNavigation(navigationButton);
		});
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
		});
	}

	@Provides
	RancourClanConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RancourClanConfig.class);
	}

	@Provides
	ClanApiClient provideApiClient(RancourClanConfig config)
	{
		return new RestClanApiClient(config.apiBaseUrl());
	}

	@Provides
	VerificationService provideVerificationService()
	{
		return new PlaceholderVerificationService();
	}

	@Provides
	AnnouncementService provideAnnouncementService()
	{
		return new PlaceholderAnnouncementService();
	}

	@Provides
	EventService provideEventService()
	{
		return new PlaceholderEventService();
	}

	@Provides
	DropService provideDropService()
	{
		return new PlaceholderDropService();
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
