package com.rancour.clan.services;

import com.rancour.clan.models.VerificationStatus;

public final class PlaceholderVerificationService implements VerificationService
{
	@Override
	public VerificationStatus getCurrentStatus()
	{
		return new VerificationStatus(false, "Account not linked");
	}

	@Override
	public VerificationStatus requestAccountLink()
	{
		return new VerificationStatus(false, "Mock link request created");
	}
}
