package com.rancour.clan.services;

import java.util.function.Consumer;

/**
 * Bridges drop workflow events from the Swing panel back into the RuneLite chat box.
 */
public final class DropChatNotifier
{
	private static final Consumer<String> NO_OP = message -> { };
	private static volatile Consumer<String> notifier = NO_OP;

	private DropChatNotifier()
	{
	}

	public static void configure(Consumer<String> chatNotifier)
	{
		notifier = chatNotifier == null ? NO_OP : chatNotifier;
	}

	public static void reset()
	{
		notifier = NO_OP;
	}

	public static void notify(String message)
	{
		if (message != null && !message.trim().isEmpty())
		{
			notifier.accept(message);
		}
	}
}
