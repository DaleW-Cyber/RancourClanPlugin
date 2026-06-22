package com.rancour.clan.models;

public class MemberProfile
{
    private final boolean verified;
    private final String status;
    private final String rsn;
    private final String rank;

    public MemberProfile(boolean verified, String status, String rsn, String rank)
    {
        this.verified = verified;
        this.status = status;
        this.rsn = rsn;
        this.rank = rank;
    }

    public boolean isVerified()
    {
        return verified;
    }

    public String getStatus()
    {
        return status;
    }

    public String getRsn()
    {
        return rsn;
    }

    public String getRank()
    {
        return rank;
    }
}
