package com.rancour.clan.models;

public final class VerificationStatus
{
	private final String state;
	private final String sessionToken;
	private final MemberProfile profile;
	private final String expiresAt;
	private final String lastCheckedAt;

	public VerificationStatus(String state, String sessionToken, MemberProfile profile, String expiresAt, String lastCheckedAt)
	{
		this.state = state;
		this.sessionToken = sessionToken;
		this.profile = profile;
		this.expiresAt = expiresAt;
		this.lastCheckedAt = lastCheckedAt;
	}

	public String getState() { return state; }
	public String getSessionToken() { return sessionToken; }
	public MemberProfile getProfile() { return profile; }
	public String getExpiresAt() { return expiresAt; }
	public String getLastCheckedAt() { return lastCheckedAt; }
	public boolean isVerified() { return "verified".equalsIgnoreCase(state) && profile != null; }
}
