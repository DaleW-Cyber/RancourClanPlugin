package com.rancour.clan.services;

public interface SessionStore
{
	String getSessionToken();
	void setSessionToken(String token);
	String getPendingVerificationId();
	void setPendingVerificationId(String verificationId);
	void clear();
}
