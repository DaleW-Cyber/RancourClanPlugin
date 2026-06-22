package com.rancour.clan.models;

public final class VerificationStatus
{
	private final boolean linked;
	private final String message;

	public VerificationStatus(boolean linked, String message)
	{
		this.linked = linked;
		this.message = message;
	}

	public boolean isLinked() { return linked; }
	public String getMessage() { return message; }
}
