package com.rancour.clan.models;

public final class DropSubmissionResult
{
	private final String id;
	private final String status;
	private final String message;

	public DropSubmissionResult(String id, String status, String message)
	{
		this.id = id;
		this.status = status;
		this.message = message;
	}

	public String getId() { return id; }
	public String getStatus() { return status; }
	public String getMessage() { return message; }
}
