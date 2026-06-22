package com.rancour.clan.services;

import com.rancour.clan.models.VerificationStatus;

public interface VerificationService
{
	VerificationStatus getCurrentStatus();

	VerificationStatus requestAccountLink();
}
