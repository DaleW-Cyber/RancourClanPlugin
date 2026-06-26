package com.rancour.clan.models;

public final class EditAnnouncementRequest
{
	private final String title;
	private final String message;
	private final String priority;
	private final String expiresAt;
	private final Boolean restricted;

	public EditAnnouncementRequest(String title, String message, String priority, String expiresAt, Boolean restricted)
	{
		this.title = title;
		this.message = message;
		this.priority = priority;
		this.expiresAt = expiresAt;
		this.restricted = restricted;
	}

	public String getTitle() { return title; }
	public String getMessage() { return message; }
	public String getPriority() { return priority; }
	public String getExpiresAt() { return expiresAt; }
	public Boolean getRestricted() { return restricted; }
}
