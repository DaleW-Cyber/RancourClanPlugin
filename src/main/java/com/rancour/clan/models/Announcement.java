package com.rancour.clan.models;

public final class Announcement
{
	private final String id;
	private final String title;
	private final String message;
	private final String priority;
	private final String createdAt;
	private final String expiresAt;
	private final String author;
	private final boolean restricted;

	public Announcement(String id, String title, String message, String priority, String createdAt,
		String expiresAt, String author, boolean restricted)
	{
		this.id = id;
		this.title = title;
		this.message = message;
		this.priority = priority;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		this.author = author;
		this.restricted = restricted;
	}

	public String getId() { return id; }
	public String getTitle() { return title; }
	public String getMessage() { return message; }
	public String getPriority() { return priority; }
	public String getCreatedAt() { return createdAt; }
	public String getExpiresAt() { return expiresAt; }
	public String getAuthor() { return author; }
	public boolean isRestricted() { return restricted; }
}
