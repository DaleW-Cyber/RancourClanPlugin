package com.rancour.clan.services;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.client.util.Text;
import com.rancour.clan.models.DropCandidate;

public final class DropDetector
{
	private static final Pattern DROP_MESSAGE = Pattern.compile("(?i)(?:valuable|untradeable) drop: ([^()]+?)(?: \\((?:[0-9,]+)\\))?$");

	public Optional<DropCandidate> fromChatMessage(String message, String rsn)
	{
		Matcher matcher = DROP_MESSAGE.matcher(Text.removeTags(message == null ? "" : message).trim());
		if (!matcher.find())
		{
			return Optional.empty();
		}
		return Optional.of(new DropCandidate(matcher.group(1).trim(), "Game chat", safe(rsn),
			Instant.now().toString(), "chat_message"));
	}

	public DropCandidate fromNpcLoot(String itemName, String source, String rsn)
	{
		return new DropCandidate(itemName, safe(source), safe(rsn), Instant.now().toString(), "npc_loot");
	}

	private static String safe(String value)
	{
		return value == null || value.trim().isEmpty() ? "Unknown" : value;
	}
}
