package com.rancour.clan;

import com.google.inject.Provides;
import com.rancour.clan.api.RancourApiClient;
import com.rancour.clan.drops.DropDetector;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(
    name = "Rancour Clan",
    description = "Clan integration panel for Rancour PvM.",
    tags = {"rancour", "clan", "pvm", "events", "drops"}
)
public class RancourClanPlugin extends Plugin
{
    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private RancourClanConfig config;

    private RancourApiClient apiClient;
    private DropDetector dropDetector;
    private RancourClanPanel panel;
    private NavigationButton navigationButton;

    @Override
    protected void startUp()
    {
        log.info("Starting Rancour Clan plugin");

        apiClient = new RancourApiClient(config);
        dropDetector = new DropDetector(apiClient);
        panel = new RancourClanPanel(apiClient);

        navigationButton = NavigationButton.builder()
            .tooltip("Rancour Clan")
            .priority(5)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navigationButton);
    }

    @Override
    protected void shutDown()
    {
        log.info("Stopping Rancour Clan plugin");

        if (navigationButton != null)
        {
            clientToolbar.removeNavigation(navigationButton);
        }
    }

    @Provides
    RancourClanConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(RancourClanConfig.class);
    }
}
