package com.rancour.clan.services;

import java.util.Set;

public interface SeenAnnouncementStore
{
	Set<String> load();
	void save(Set<String> announcementIds);
}
