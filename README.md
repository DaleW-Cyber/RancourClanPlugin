# Rancour Clan Plugin

An early-release RuneLite plugin for Rancour clan verification, announcements, events, drop submissions, team finding, and staff workflows. All external communication goes through the configured Rancour REST API. The plugin contains no Discord bot token, Google credentials, Railway secret, or direct Google Sheets integration.

## Current MVP

- Discord link-code generation and verification status refresh
- RuneLite config-backed API session persistence
- Verified profile display: Discord name, active RSN, trusted linked RSNs, clan rank, staff status, expiry, and last checked time
- Compact announcement feed that shows only title/body, with loading, empty, refresh, error, and optional non-duplicating chat notifications
- Discord-backed, API role-filtered read-only event list
- Valuable/untradeable game-chat and high-value NPC-loot detection with approved-catalogue filtering and 30-second duplicate prevention
- Explicit drop confirmation before API submission
- Team Finder with verified Join/Leave actions, host display, and joined member names
- Staff-only menu with compact announcement creation/deletion, Drops Panel toggle, and team edit/close controls
- Explicit mock mode for local development

The required backend contract is documented in [docs/API_CONTRACT.md](docs/API_CONTRACT.md). Release readiness is tracked in [docs/EARLY_RELEASE_CHECKLIST.md](docs/EARLY_RELEASE_CHECKLIST.md).

## Requirements

- Java Development Kit 11
- Git
- Internet access for Gradle dependencies and live API testing

The Gradle 8.10 wrapper is included, so a system Gradle installation is not required.

## Build

```bash
./gradlew clean build
```

Windows PowerShell:

```powershell
.\gradlew.bat clean build
```

## Run RuneLite locally

```bash
./gradlew run
```

Windows PowerShell:

```powershell
.\gradlew.bat run
```

RuneLite starts in developer mode with `Rancour Clan` loaded as an external plugin. Enable it in RuneLite configuration if necessary, then open the `R` sidebar icon.

## IntelliJ IDEA

1. Install a Java 11 JDK.
2. Open the repository as a Gradle project.
3. Select the included Gradle wrapper.
4. Set both the project SDK and Gradle JVM to Java 11.
5. Wait for Gradle synchronization.
6. Run the Gradle `run` task or `com.rancour.clan.RancourClanPluginTest` from `src/test/java`.

Do not add a RuneLite client JAR manually.

## Configuration

`API base URL` controls the only network destination used by the plugin. It defaults to:

```text
https://api.rancourpvm.com
```

The value is trimmed and normalized so trailing slashes do not create duplicate slashes in request URLs. Use **Test API Connection** on the Verification page to call `GET /health` and display the result without creating a verification attempt.

When running with `./gradlew run`, API diagnostics are written to the Gradle/IntelliJ run console. Verification start logs the complete request URL. Failed HTTP responses log the status and a redacted, size-limited response body, while connection failures include their stack trace. Authorization values, session tokens, and common secret fields are never intentionally logged.

`Show announcement notifications in chat` enables short `[Rancour]` game-chat notices for announcement IDs not previously seen on this RuneLite profile. `Minimum announcement priority` can be `normal`, `high`, or `urgent`. Seen IDs are stored locally; announcement bodies and session credentials are never written to chat.

`Enable automatic refresh` is on by default. `Refresh interval seconds` defaults to 60 and is clamped to a minimum of 30 seconds. Background refresh updates verification, announcements, events, and teams without blocking the RuneLite client thread. Manual Refresh buttons remain available as a fallback.

`Mock mode` is disabled by default. When enabled, every page uses clearly labelled local mock data and an in-memory mock session. Mock mode never writes its session token to RuneLite configuration.

`Minimum drop value` defaults to 1,000,000 GP and controls which NPC loot events create a confirmation prompt. Game-chat valuable/untradeable notifications are also detected independently of this threshold.

The live verification session token and pending verification ID are stored under the RuneLite `rancourclan` configuration group. They are API-issued client credentials only; no Discord, Railway, or Google secret is stored.

## User flows

### Verification

1. Select `Generate Link Code`, then optionally use `Copy Code`.
2. Use `/plugin_link CODE` in Discord before expiry.
3. Select `Refresh Status`.
4. The returned API session and member profile are stored/displayed.

Protected actions such as Team Join, Drop Submit, and Staff tools require the stored API session token and send `Authorization: Bearer <sessionToken>`. If that token is missing, the panel shows `Verification session missing. Please refresh verification or link again.` If the API reports an expired/revoked session, RuneLite clears the local session and asks the user to refresh or link again.

The plugin compares the currently logged-in RuneLite account with `linkedRsns` from the API. Unknown accounts show a warning and cannot confirm a drop. When logged out, the panel asks the player to log in before confirming the active RSN.

### Drops

The detector watches RuneLite game chat for valuable/untradeable notifications and RuneLite NPC loot events above the configured total GE-value threshold. Detected items are filtered against the approved Rancour drop catalogue returned by `GET /plugin/settings`; unknown high-value items are ignored and never create a pending candidate. A recognized candidate appears on the Drops page. The player must select `Confirm Submit`; detection alone never sends data.

Each submission includes item name, source, current RuneLite RSN, UTC timestamp, and detection method. Identical item/source/RSN detections are suppressed for 30 seconds. The API independently requires that RSN to be in the verified profile's trusted linked-RSN set and rejects items outside the approved catalogue. Discord does not need to recognise the drop first; RuneLite detects it, then the API validates it.

Staff can enable or disable member drop submissions from `Staff -> Drops Panel`. The setting is loaded through `GET /plugin/settings`, changed through `POST /plugin/staff/settings/drops-panel`, and enforced server-side. When disabled, the Drops tab is hidden and the Drops page shows `Drop submissions are currently disabled.`

### Staff access

The Staff page is visible only after `/plugin/me` or verification status returns `staff=true`. The API derives this value from the verified Discord role IDs and `STAFF_ROLE_IDS`; RuneLite has no local staff list. Discord role updates are synchronized by the bot, so the next status refresh removes the Staff page after a staff role is lost. Client-side visibility is convenience only; every staff endpoint also enforces authorization server-side. The current RuneLite Staff menu shows working announcement creation/deletion, the Drops Panel toggle, and Staff -> Teams edit/close controls; pending drop approval remains in Discord.

Staff announcement expiry is selected from fixed options: 1 hour, 6 hours, 12 hours, 1 day, 2 days, 3 days, or 7 days. RuneLite calculates `expiresAt` in UTC, and the API rejects any expiry longer than seven days.

### Event visibility

The API, not RuneLite, filters events using the Discord roles stored on the verified profile. Unverified users receive only public events. Verified members receive member events and restricted events matching their Discord roles; staff events require API-derived staff status. RuneLite Events are currently read-only. Joining/leaving events is handled in Discord.

### Teams

Team Finder shows active teams returned by the API, including host, joined members, world, capacity, and voice requirement. Teams expire two hours after creation. Full teams remain visible for five minutes after reaching capacity, then disappear from normal Team Finder responses. Staff can manage active teams from `Staff -> Teams`, including editing activity/capacity/world/voice/tags/status and closing teams with confirmation.

## Architecture

```text
src/main/java/com/rancour/clan/
|-- RancourClanPlugin.java       RuneLite lifecycle and chat subscriber
|-- api/                         Async OkHttp transport and mock API
|-- config/                      User-facing RuneLite configuration
|-- models/                      Typed API request/response models
|-- services/                    Session, authorization, and feature logic
`-- ui/                          Swing pages and async UI states
```

`ClanApiClient` is the transport boundary. `RestClanApiClient` uses RuneLite's shared `OkHttpClient` asynchronously and parses typed Gson models. UI components only call services and marshal completion updates back to Swing's event thread. No HTTP request blocks the RuneLite client thread.

`ApiServices` centralizes session checks. Drops and team membership require a stored plugin session token; Staff operations additionally require the current profile to report `staff=true`. The Railway API remains the final authority.

## What works without the backend

- Plugin compilation and local RuneLite launch
- All six pages and their loading/error/empty layouts
- Mock verification, announcements, events, drops, teams, and staff announcement creation
- Chat drop candidate detection, duplicate prevention, confirmation, and dismissal
- Client-side verified/staff action guards

## Remaining integration work

- Staff-approved Discord commands/workflow to add, set-primary, and revoke linked alts
- Discord event participation write-back
- Approved plugin-drop integration with the legacy Google Sheet/log workflow
- Staff event-cache refresh endpoint
- Staff team lock endpoint

See [docs/UI_QA.md](docs/UI_QA.md) for the normal-sidebar-width manual check.

## Plugin Hub conventions

- Java 11 bytecode via `options.release=11`
- RuneLite client remains `compileOnly`
- Local launcher remains in `src/test`
- Plugin entry point is declared in `runelite-plugin.properties`
- Gradle `run` enables developer mode and assertions
- No bundled external-service credentials or direct Discord/Sheets clients

## License

See [LICENSE](LICENSE).
