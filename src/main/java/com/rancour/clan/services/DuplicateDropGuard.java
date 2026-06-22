package com.rancour.clan.services;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.rancour.clan.models.DropCandidate;

public final class DuplicateDropGuard
{
	private final Duration window;
	private final Clock clock;
	private final Map<String, Instant> recent = new HashMap<>();

	public DuplicateDropGuard(Duration window)
	{
		this(window, Clock.systemUTC());
	}

	DuplicateDropGuard(Duration window, Clock clock)
	{
		this.window = window;
		this.clock = clock;
	}

	public synchronized boolean accept(DropCandidate candidate)
	{
		Instant now = clock.instant();
		Iterator<Instant> values = recent.values().iterator();
		while (values.hasNext())
		{
			if (values.next().plus(window).isBefore(now))
			{
				values.remove();
			}
		}
		String key = candidate.getItemName().toLowerCase() + "|" + candidate.getSource().toLowerCase() + "|" + candidate.getRsn().toLowerCase();
		Instant previous = recent.put(key, now);
		return previous == null || previous.plus(window).isBefore(now);
	}
}
