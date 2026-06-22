package com.rancour.clan.ui;

import static org.junit.Assert.assertEquals;

import java.awt.Container;
import org.junit.Test;
import com.rancour.clan.services.PlaceholderAnnouncementService;
import com.rancour.clan.services.PlaceholderDropService;
import com.rancour.clan.services.PlaceholderEventService;
import com.rancour.clan.services.PlaceholderVerificationService;

public class RancourClanPanelTest
{
	@Test
	public void createsNavigationForAllPlaceholderPages()
	{
		RancourClanPanel panel = new RancourClanPanel(
			new PlaceholderVerificationService(),
			new PlaceholderAnnouncementService(),
			new PlaceholderEventService(),
			new PlaceholderDropService()
		);

		Container navigation = (Container) panel.getComponent(0);
		assertEquals(4, navigation.getComponentCount());
	}
}
