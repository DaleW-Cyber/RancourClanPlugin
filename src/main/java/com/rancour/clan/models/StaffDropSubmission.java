package com.rancour.clan.models;

public final class StaffDropSubmission
{
	private final String id;
	private final String itemName;
	private final String source;
	private final String rsn;
	private final String submittedAt;
	private final String status;

	public StaffDropSubmission(String id, String itemName, String source, String rsn, String submittedAt, String status)
	{
		this.id = id;
		this.itemName = itemName;
		this.source = source;
		this.rsn = rsn;
		this.submittedAt = submittedAt;
		this.status = status;
	}

	public String getId() { return id; }
	public String getItemName() { return itemName; }
	public String getSource() { return source; }
	public String getRsn() { return rsn; }
	public String getSubmittedAt() { return submittedAt; }
	public String getStatus() { return status; }
}
