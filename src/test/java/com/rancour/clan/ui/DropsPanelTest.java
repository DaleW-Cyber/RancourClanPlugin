package com.rancour.clan.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import javax.swing.JButton;
import org.junit.Test;
import com.rancour.clan.api.MockClanApiClient;
import com.rancour.clan.models.DropCandidate;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.services.ApiServices;
import com.rancour.clan.services.InMemorySessionStore;
import com.rancour.clan.services.VerificationService;

public class DropsPanelTest
{
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
		panel.offerCandidate(new DropCandidate(
			"Twisted bow",
			"Chambers of Xeric",
			"Unlinked Alt",
			"2026-06-22T16:50:00Z",
			"npc_loot"
		));

		assertFalse(button(panel, "Confirm Submit").isEnabled());
	}

	@Test
	public void linkedAltCanConfirmSubmission()
	{
		MockClanApiClient api = new MockClanApiClient();
		VerificationService verification = ApiServices.verification(api, new InMemorySessionStore());
		DropsPanel panel = new DropsPanel(ApiServices.drops(api, verification), () -> "Linked Alt");
		panel.setProfile(new MemberProfile(
			"Discord",
			"Main RSN",
			Arrays.asList("Main RSN", "Linked Alt"),
			"Member",
			false,
			"later",
			"now"
		));
		panel.offerCandidate(new DropCandidate(
			"Twisted bow",
			"Chambers of Xeric",
			"Linked Alt",
			"2026-06-22T16:50:00Z",
			"npc_loot"
		));

		assertTrue(button(panel, "Confirm Submit").isEnabled());
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
