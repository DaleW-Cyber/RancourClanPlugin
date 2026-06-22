package com.rancour.clan.services;

public interface SeenTeamReadyStore
{
	boolean isSeen(String teamId);
	void markSeen(String teamId);
}
