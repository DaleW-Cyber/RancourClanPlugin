package com.rancour.clan.services;

public final class InMemorySessionStore implements SessionStore
{
	private String sessionToken = "";
	private String verificationId = "";

	@Override public String getSessionToken() { return sessionToken; }
	@Override public void setSessionToken(String token) { sessionToken = token == null ? "" : token; }
	@Override public String getPendingVerificationId() { return verificationId; }
	@Override public void setPendingVerificationId(String id) { verificationId = id == null ? "" : id; }
	@Override public void clear() { sessionToken = ""; verificationId = ""; }
}
