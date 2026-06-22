package com.rancour.clan.models;

public final class VerificationStartResponse
{
	private final String code;
	private final String verificationId;
	private final String expiresAt;

	public VerificationStartResponse(String code, String verificationId, String expiresAt)
	{
		this.code = code;
		this.verificationId = verificationId;
		this.expiresAt = expiresAt;
	}

	public String getCode() { return code; }
	public String getVerificationId() { return verificationId; }
	public String getExpiresAt() { return expiresAt; }
}
