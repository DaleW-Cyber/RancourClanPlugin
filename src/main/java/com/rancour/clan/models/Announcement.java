package com.rancour.clan.models;

public final class Announcement
{
	private final String title;
	private final String body;
	private final String publishedAt;

	public Announcement(String title, String body, String publishedAt)
	{
		this.title = title;
		this.body = body;
		this.publishedAt = publishedAt;
	}

	public String getTitle() { return title; }
	public String getBody() { return body; }
	public String getPublishedAt() { return publishedAt; }
}
