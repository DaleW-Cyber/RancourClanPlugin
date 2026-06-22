package com.rancour.clan.models;

public final class CreateAnnouncementRequest
{
	private final String title;
	private final String message;
	private final String priority;
	private final String expiresAt;

	public CreateAnnouncementRequest(String title, String message, String priority, String expiresAt)
	{
		this.title = title;
		this.message = message;
		this.priority = priority;
		this.expiresAt = expiresAt;
	}

	public String getTitle() { return title; }
	public String getMessage() { return message; }
	public String getPriority() { return priority; }
	public String getExpiresAt() { return expiresAt; }
}
