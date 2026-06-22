package com.rancour.clan.models;

import java.time.Instant;

public class Announcement
{
    private final String title;
    private final String message;
    private final String priority;
    private final Instant createdAt;

    public Announcement(String title, String message, String priority, Instant createdAt)
    {
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.createdAt = createdAt;
    }

    public String getTitle()
    {
        return title;
    }

    public String getMessage()
    {
        return message;
    }

    public String getPriority()
    {
        return priority;
    }

    public Instant getCreatedAt()
    {
        return createdAt;
    }
}
