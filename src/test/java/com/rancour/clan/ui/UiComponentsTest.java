package com.rancour.clan.ui;

import static org.junit.Assert.assertTrue;

import javax.swing.JPanel;
import org.junit.Test;

public class UiComponentsTest
{
	@Test
	public void longDetailsStayWithinNormalSidebarContentWidth()
	{
		JPanel card = UiComponents.detailsCard(
			"A deliberately long event title that must wrap rather than stretch the sidebar",
			"A long description that should remain readable inside the standard RuneLite side panel width.",
			"Required roles",
			"Role one, Role two, Role three, Role four, Role five"
		);

		assertTrue("Preferred width was " + card.getPreferredSize().width,
			card.getPreferredSize().width <= 210);
	}

	@Test
	public void unbrokenValuesStayWithinNormalSidebarContentWidth()
	{
		JPanel card = UiComponents.detailsCard(
			"Verification",
			"",
			"Discord command",
			"/plugin_link_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
		);

		assertTrue("Preferred width was " + card.getPreferredSize().width,
			card.getPreferredSize().width <= 210);
	}
}
