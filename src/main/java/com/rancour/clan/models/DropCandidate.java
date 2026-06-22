package com.rancour.clan.models;

public final class DropCandidate
{
	private final String itemName;
	private final String source;
	private final String rsn;
	private final String detectedAt;
	private final String detectionMethod;

	public DropCandidate(String itemName, String source, String rsn, String detectedAt, String detectionMethod)
	{
		this.itemName = itemName;
		this.source = source;
		this.rsn = rsn;
		this.detectedAt = detectedAt;
		this.detectionMethod = detectionMethod;
	}

	public String getItemName() { return itemName; }
	public String getSource() { return source; }
	public String getRsn() { return rsn; }
	public String getDetectedAt() { return detectedAt; }
	public String getDetectionMethod() { return detectionMethod; }
	public DropSubmission toSubmission() { return new DropSubmission(itemName, source, rsn, detectedAt, detectionMethod); }
}
