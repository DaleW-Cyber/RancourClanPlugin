package com.rancour.clan.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.After;
import org.junit.Test;
import com.rancour.clan.api.MockClanApiClient;
import com.rancour.clan.models.DropCandidate;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.services.ApiServices;
import com.rancour.clan.services.DropChatNotifier;
import com.rancour.clan.services.InMemorySessionStore;
import com.rancour.clan.services.VerificationService;

public class DropsPanelTest
{
	@After
	public void resetNotifier()
	{
		DropChatNotifier.reset();
	}

	@Test
	public void submissionRequiresTheActiveRuneLiteAccountToBeLinked()
	{
		MockClanApiClient api = new MockClanApiClient();
		VerificationService verification = ApiServices.verification(api, new InMemorySessionStore());
		DropsPanel panel = new DropsPanel(ApiServices.drops(api, verification), () -> "Unlinked Alt");
		panel.setProfile(new MemberProfile(
			"Discord",
			"Main RSN",
			Arrays.asList("Main RSN", "Linked Alt"),
			"Member",
			false,
			"later",
			"now"
		));
		panel.offerCandidate(candidate("Unlinked Alt"));

		assertFalse(button(panel, "Confirm Submit").isEnabled());
	}

	@Test
	public void linkedAltCanConfirmSubmission()
	{
		MockClanApiClient api = new MockClanApiClient();
		VerificationService verification = ApiServices.verification(api, new InMemorySessionStore());
		DropsPanel panel = new DropsPanel(ApiServices.drops(api, verification), () -> "Linked Alt");
		panel.setProfile(linkedProfile());
		panel.offerCandidate(candidate("Linked Alt"));

		assertTrue(button(panel, "Confirm Submit").isEnabled());
	}

	@Test
	public void candidateAndSuccessfulSubmissionNotifyTheGameChat() throws Exception
	{
		List<String> messages = new ArrayList<>();
		DropChatNotifier.configure(messages::add);
		MockClanApiClient api = new MockClanApiClient();
		VerificationService verification = ApiServices.verification(api, new InMemorySessionStore());
		verification.refreshProfile().toCompletableFuture().get();
		DropsPanel panel = new DropsPanel(ApiServices.drops(api, verification), () -> "Linked Alt");
		panel.setProfile(linkedProfile());

		panel.offerCandidate(candidate("Linked Alt"));
		button(panel, "Confirm Submit").doClick();
		SwingUtilities.invokeAndWait(() -> { });

		assertEquals(2, messages.size());
		assertEquals("Rancour PvM: A drop is ready to submit. Check the Rancour PvM plugin panel.", messages.get(0));
		assertEquals("Rancour PvM: Your drop has been submitted successfully.", messages.get(1));
	}

	private static DropCandidate candidate(String rsn)
	{
		return new DropCandidate(
			"Twisted bow",
			"Chambers of Xeric",
			rsn,
			"2026-06-22T16:50:00Z",
			"npc_loot"
		);
	}

	private static MemberProfile linkedProfile()
	{
		return new MemberProfile(
			"Discord",
			"Main RSN",
			Arrays.asList("Main RSN", "Linked Alt"),
			"Member",
			false,
			"later",
			"now"
		);
	}

	private static JButton button(Container container, String text)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof JButton && text.equals(((JButton) component).getText()))
			{
				return (JButton) component;
			}
			if (component instanceof Container)
			{
				try
				{
					return button((Container) component, text);
				}
				catch (AssertionError ignored)
				{
					// Continue searching sibling containers.
				}
			}
		}
		throw new AssertionError("Button not found: " + text);
	}
}
