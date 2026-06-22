package com.rancour.clan.services;

import java.util.concurrent.CompletionStage;
import com.rancour.clan.models.DropSubmission;
import com.rancour.clan.models.DropSubmissionResult;

public interface DropService
{
	CompletionStage<DropSubmissionResult> submit(DropSubmission submission);
}
