package com.rancour.clan.models;

import java.time.Instant;

public class ClanEvent
{
    private final String title;
    private final String description;
    private final Instant startTime;

    public ClanEvent(String title, String description, Instant startTime)
    {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public Instant getStartTime()
    {
        return startTime;
    }
}
