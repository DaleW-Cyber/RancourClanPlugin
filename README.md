# Rancour Clan Plugin

RuneLite side-panel plugin for Rancour PvM clan integrations.

## Current milestone

Milestone 1 creates a basic RuneLite plugin skeleton with placeholder sections for:

- Verification
- Announcements
- Events
- Drop Submission

No live API calls are implemented yet.

## Intended future behaviour

The plugin will eventually connect to a Rancour API service that integrates with the Discord bot and clan data sources.

Planned first features:

1. Verified member linking through Discord.
2. Announcement feed from staff-created clan announcements.
3. Event list linked to Discord events.
4. Automatic drop submission prompt for eligible drops.

## Project structure

```text
src/main/java/com/rancour/clan/
├── RancourClanPlugin.java
├── RancourClanConfig.java
├── RancourClanPanel.java
├── api/
│   └── RancourApiClient.java
├── drops/
│   └── DropDetector.java
└── models/
    ├── Announcement.java
    ├── ClanEvent.java
    └── MemberProfile.java
```

## Local development

This project is structured as an external RuneLite plugin with a local test launcher.

Build:

```bash
gradle build
```

Run RuneLite with the plugin loaded:

```bash
gradle run
```

In IntelliJ IDEA:

1. Open the repository as a Gradle project.
2. Use Java 11.
3. Run `com.rancour.clan.RancourClanPluginTest` from `src/test/java`.
4. Open RuneLite and confirm the `Rancour Clan` side panel appears.

## Configuration

The plugin has one initial configuration value:

```text
API Base URL
```

Default:

```text
https://api.rancourpvm.com
```

This is a placeholder until the Railway plugin API service is created.

## Next development step

Confirm the plugin launches locally, then add the first real API-backed feature: verified member linking.
