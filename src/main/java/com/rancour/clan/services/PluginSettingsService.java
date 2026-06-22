package com.rancour.clan.services;

import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.PluginSettings;

public interface PluginSettingsService
{
	CompletionStage<PluginSettings> loadSettings();
}
