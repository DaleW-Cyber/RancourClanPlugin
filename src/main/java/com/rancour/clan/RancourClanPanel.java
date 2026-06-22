package com.rancour.clan;

import com.rancour.clan.api.RancourApiClient;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.PluginPanel;

public class RancourClanPanel extends PluginPanel
{
    private final RancourApiClient apiClient;
    private final JTextArea outputArea = new JTextArea();

    public RancourClanPanel(RancourApiClient apiClient)
    {
        super(false);
        this.apiClient = apiClient;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        buttonPanel.add(new JLabel("Rancour Clan Plugin"));
        buttonPanel.add(createButton("Verification", this::showVerification));
        buttonPanel.add(createButton("Announcements", this::showAnnouncements));
        buttonPanel.add(createButton("Events", this::showEvents));
        buttonPanel.add(createButton("Drop Submission", this::showDropSubmission));

        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setText("Select a section to begin.");

        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
    }

    private JButton createButton(String label, Runnable action)
    {
        JButton button = new JButton(label);
        button.addActionListener(event -> action.run());
        return button;
    }

    private void showVerification()
    {
        outputArea.setText(apiClient.getVerificationStatus());
    }

    private void showAnnouncements()
    {
        outputArea.setText(apiClient.getAnnouncements());
    }

    private void showEvents()
    {
        outputArea.setText(apiClient.getEvents());
    }

    private void showDropSubmission()
    {
        outputArea.setText("Drop submission prompt placeholder.\n\nFuture behaviour:\n- Detect eligible drops\n- Show a confirmation prompt\n- Submit to the Rancour API\n- Send to Discord staff review");
    }
}
