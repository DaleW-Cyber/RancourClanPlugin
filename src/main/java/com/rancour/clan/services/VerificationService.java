package com.rancour.clan.services;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import com.rancour.clan.models.MemberProfile;
import com.rancour.clan.models.VerificationStartResponse;
import com.rancour.clan.models.VerificationStatus;

public interface VerificationService
{
	CompletionStage<VerificationStartResponse> generateLinkCode();
	CompletionStage<VerificationStatus> refreshStatus();
	MemberProfile getCurrentProfile();
	boolean isVerified();
	String getSessionToken();
	void addProfileListener(Consumer<MemberProfile> listener);
	void clearSession();
}
