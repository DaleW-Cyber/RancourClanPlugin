package com.rancour.clan.ui;

@FunctionalInterface
interface ClipboardWriter
{
	void copy(String value);
}
