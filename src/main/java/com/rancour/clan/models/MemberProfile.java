package com.rancour.clan.models;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class MemberProfile
{
	private final String discordName;
	private final String rsn;
	private final String primaryRsn;
	private final List<String> linkedRsns;
	private final String clanRank;
	private final boolean staff;
	private final String expiresAt;
	private final String lastCheckedAt;

	public MemberProfile(String discordName, String rsn, String clanRank, boolean staff,
		String expiresAt, String lastCheckedAt)
	{
		this(discordName, rsn, Collections.singletonList(rsn), clanRank, staff, expiresAt, lastCheckedAt);
	}

	public MemberProfile(String discordName, String primaryRsn, List<String> linkedRsns,
		String clanRank, boolean staff, String expiresAt, String lastCheckedAt)
	{
		this.discordName = discordName;
		this.rsn = primaryRsn;
		this.primaryRsn = primaryRsn;
		this.linkedRsns = linkedRsns;
		this.clanRank = clanRank;
		this.staff = staff;
		this.expiresAt = expiresAt;
		this.lastCheckedAt = lastCheckedAt;
	}

	public String getDiscordName() { return discordName; }
	public String getRsn() { return rsn; }
	public String getPrimaryRsn() { return primaryRsn == null || primaryRsn.isEmpty() ? rsn : primaryRsn; }
	public List<String> getLinkedRsns()
	{
		return linkedRsns == null || linkedRsns.isEmpty()
			? Collections.singletonList(getPrimaryRsn())
			: linkedRsns;
	}
	public boolean isLinkedRsn(String accountName)
	{
		String candidate = normalize(accountName);
		return !candidate.isEmpty() && getLinkedRsns().stream()
			.map(MemberProfile::normalize)
			.anyMatch(candidate::equals);
	}
	public String getClanRank() { return clanRank; }
	public boolean isStaff() { return staff; }
	public String getExpiresAt() { return expiresAt; }
	public String getLastCheckedAt() { return lastCheckedAt; }

	private static String normalize(String value)
	{
		return value == null ? "" : value.replace('_', ' ').trim().replaceAll("\\s+", " ")
			.toLowerCase(Locale.ROOT);
	}
}
