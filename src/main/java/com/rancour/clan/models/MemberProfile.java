package com.rancour.clan.models;

public final class MemberProfile
{
	private final String discordName;
	private final String rsn;
	private final String clanRank;
	private final boolean staff;
	private final String expiresAt;
	private final String lastCheckedAt;

	public MemberProfile(String discordName, String rsn, String clanRank, boolean staff,
		String expiresAt, String lastCheckedAt)
	{
		this.discordName = discordName;
		this.rsn = rsn;
		this.clanRank = clanRank;
		this.staff = staff;
		this.expiresAt = expiresAt;
		this.lastCheckedAt = lastCheckedAt;
	}

	public String getDiscordName() { return discordName; }
	public String getRsn() { return rsn; }
	public String getClanRank() { return clanRank; }
	public boolean isStaff() { return staff; }
	public String getExpiresAt() { return expiresAt; }
	public String getLastCheckedAt() { return lastCheckedAt; }
}
