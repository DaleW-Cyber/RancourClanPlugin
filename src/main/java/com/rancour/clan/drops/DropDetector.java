package com.rancour.clan.drops;

import com.rancour.clan.api.RancourApiClient;

public class DropDetector
{
    private final RancourApiClient apiClient;

    public DropDetector(RancourApiClient apiClient)
    {
        this.apiClient = apiClient;
    }

    public String handleDetectedDrop(String boss, String item)
    {
        return apiClient.submitDrop(boss, item);
    }
}
