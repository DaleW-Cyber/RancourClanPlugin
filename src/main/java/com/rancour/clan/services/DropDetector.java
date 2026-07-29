package com.rancour.clan.services;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.client.util.Text;
import com.rancour.clan.models.DropCandidate;

public final class DropDetector
{
	private static final Pattern DROP_MESSAGE = Pattern.compile(
		"(?i)(?:valuable|untradeable) drop: ([^()]+?)(?: \\((?:[0-9,]+)\\))?$"
	);
	private static final Pattern CHAMBERS_SPECIAL_LOOT = Pattern.compile(
		"(?i)^.+? received special loot from a raid: (.+?)(?: \\([^)]*\\))?\\.?$"
	);

	public Optional<DropCandidate> fromChatMessage(String message, String rsn)
	{
		String cleanMessage = Text.removeTags(message == null ? "" : message).trim();
		Matcher matcher = DROP_MESSAGE.matcher(cleanMessage);
		if (matcher.find())
		{
			return Optional.of(candidate(matcher.group(1), "Game chat", rsn, "chat_message"));
		}

		Matcher chambersMatcher = CHAMBERS_SPECIAL_LOOT.matcher(cleanMessage);
		if (chambersMatcher.find())
		{
			return Optional.of(candidate(chambersMatcher.group(1), "Chambers of Xeric", rsn, "chat_message"));
		}

		return Optional.empty();
	}

	public DropCandidate fromNpcLoot(String itemName, String source, String rsn)
	{
		return candidate(itemName, source, rsn, "npc_loot");
	}

	public DropCandidate fromLootTracker(String itemName, String source, String rsn)
	{
		return candidate(itemName, source, rsn, "loot_tracker");
	}

	private static DropCandidate candidate(String itemName, String source, String rsn, String method)
	{
		return new DropCandidate(safe(itemName), safe(source), safe(rsn), Instant.now().toString(), method);
	}

	private static String safe(String value)
	{
		return value == null || value.trim().isEmpty() ? "Unknown" : value.trim();
	}
}
