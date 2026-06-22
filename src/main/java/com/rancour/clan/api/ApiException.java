package com.rancour.clan.api;

public final class ApiException extends RuntimeException
{
	private final int statusCode;

	public ApiException(String message)
	{
		this(message, 0);
	}

	public ApiException(String message, int statusCode)
	{
		super(message);
		this.statusCode = statusCode;
	}

	public int getStatusCode()
	{
		return statusCode;
	}
}
