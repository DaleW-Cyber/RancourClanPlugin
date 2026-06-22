package com.rancour.clan.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import org.junit.Test;
import com.rancour.clan.models.DropCandidate;

public class DropDetectorTest
{
	@Test
	public void detectsValuableDropChatMessage()
	{
		DropDetector detector = new DropDetector();
		DropCandidate candidate = detector.fromChatMessage("Valuable drop: Twisted bow (1)", "Test RSN").orElseThrow(AssertionError::new);
		assertEquals("Twisted bow", candidate.getItemName());
		assertEquals("Test RSN", candidate.getRsn());
		assertEquals("chat_message", candidate.getDetectionMethod());
	}

	@Test
	public void ignoresOrdinaryChat()
	{
		assertFalse(new DropDetector().fromChatMessage("Welcome to RuneScape.", "Test RSN").isPresent());
	}

	@Test
	public void createsNpcLootCandidate()
	{
		DropCandidate candidate = new DropDetector().fromNpcLoot("Scythe of vitur", "Verzik Vitur", "Test RSN");
		assertEquals("Scythe of vitur", candidate.getItemName());
		assertEquals("Verzik Vitur", candidate.getSource());
		assertEquals("npc_loot", candidate.getDetectionMethod());
	}

	@Test
	public void suppressesDuplicateCandidateWithinWindow()
	{
		DuplicateDropGuard guard = new DuplicateDropGuard(Duration.ofSeconds(30));
		DropCandidate candidate = new DropCandidate("Twisted bow", "Game chat", "Test RSN", "now", "chat_message");
		assertTrue(guard.accept(candidate));
		assertFalse(guard.accept(candidate));
	}
}
