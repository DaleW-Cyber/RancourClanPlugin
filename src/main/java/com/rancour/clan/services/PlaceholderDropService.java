package com.rancour.clan.services;

import com.rancour.clan.models.DropSubmission;

public final class PlaceholderDropService implements DropService
{
	@Override
	public String submit(DropSubmission submission)
	{
		return "Mock submission ready for " + submission.getItemName();
	}
}
