package com.rancour.clan.models;

public final class DropSubmission
{
	private final String itemName;
	private final String source;

	public DropSubmission(String itemName, String source)
	{
		this.itemName = itemName;
		this.source = source;
	}

	public String getItemName() { return itemName; }
	public String getSource() { return source; }
}
