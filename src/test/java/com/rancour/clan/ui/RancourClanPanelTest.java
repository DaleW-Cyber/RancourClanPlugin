package com.rancour.clan.ui;

import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import org.junit.Test;
import com.rancour.clan.api.MockClanApiClient;
import com.rancour.clan.services.ApiServices;
import com.rancour.clan.services.InMemorySessionStore;
import com.rancour.clan.services.VerificationService;

public class RancourClanPanelTest
{
	@Test
	public void createsNavigationForEveryMvpPage()
	{
		MockClanApiClient api = new MockClanApiClient();
		VerificationService verification = ApiServices.verification(api, new InMemorySessionStore());
		RancourClanPanel panel = new RancourClanPanel(
			verification,
			ApiServices.announcements(api, verification),
			ApiServices.events(api, verification),
			ApiServices.drops(api, verification),
			ApiServices.teams(api, verification),
			ApiServices.staff(api, verification),
			true);

		Set<String> buttons = new HashSet<>();
		collectButtonLabels(panel, buttons);
		assertTrue(buttons.contains("Verify"));
		assertTrue(buttons.contains("News"));
		assertTrue(buttons.contains("Events"));
		assertTrue(buttons.contains("Drops"));
		assertTrue(buttons.contains("Teams"));
		assertTrue(buttons.contains("Staff"));
	}

	private static void collectButtonLabels(Container container, Set<String> labels)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof JButton)
			{
				labels.add(((JButton) component).getText());
			}
			if (component instanceof Container)
			{
				collectButtonLabels((Container) component, labels);
			}
		}
	}
}
