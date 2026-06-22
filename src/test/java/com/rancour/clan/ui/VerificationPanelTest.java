package com.rancour.clan.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.Test;
import com.rancour.clan.models.ApiHealth;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.VerificationStartResponse;
import com.rancour.clan.models.VerificationStatus;
import com.rancour.clan.services.VerificationService;

public class VerificationPanelTest
{
	@Test
	public void copyButtonCopiesOnlyTheVerificationCode() throws Exception
	{
		AtomicReference<String> copied = new AtomicReference<>();
		VerificationPanel panel = new VerificationPanel(new LinkService(), copied::set);

		button(panel, "Generate Code").doClick();
		SwingUtilities.invokeAndWait(() -> { });
		button(panel, "Copy Code").doClick();

		assertEquals("ABCD1234", copied.get());
		assertFalse(copied.get().contains("session-secret"));
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

	private static final class LinkService implements VerificationService
	{
		@Override public CompletionStage<ApiHealth> testConnection() { return CompletableFuture.completedFuture(new ApiHealth("ok")); }
		@Override public CompletionStage<VerificationStartResponse> generateLinkCode() { return CompletableFuture.completedFuture(new VerificationStartResponse("ABCD1234", "attempt", "later")); }
		@Override public CompletionStage<VerificationStatus> refreshStatus() { return CompletableFuture.completedFuture(new VerificationStatus("pending", null, null, "later", "now")); }
		@Override public MemberProfile getCurrentProfile() { return null; }
		@Override public boolean isVerified() { return false; }
		@Override public String getSessionToken() { return "session-secret"; }
		@Override public void addProfileListener(Consumer<MemberProfile> listener) { }
		@Override public void clearSession() { }
	}
}
