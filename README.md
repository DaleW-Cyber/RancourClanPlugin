# Rancour Clan Plugin

An early-release RuneLite plugin for Rancour clan verification, announcements, events, drop submissions, team finding, and staff workflows. All external communication goes through the configured Rancour REST API. The plugin contains no Discord bot token, Google credentials, Railway secret, or direct Google Sheets integration.

## Current MVP

- Discord link-code generation and verification status refresh
- RuneLite config-backed API session persistence
- Verified profile display: Discord name, RSN, clan rank, staff status, expiry, and last checked time
- Public and restricted announcement feed with loading, empty, refresh, and error states
- Discord-backed event list with verified Join and Leave actions
- Valuable/untradeable game-chat and high-value NPC-loot detection with 30-second duplicate prevention
- Explicit drop confirmation before API submission
- Team Finder with verified Join and Leave actions
- Staff-only navigation for announcements and pending-drop review
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

`Mock mode` is disabled by default. When enabled, every page uses clearly labelled local mock data and an in-memory mock session. Mock mode never writes its session token to RuneLite configuration.

`Minimum drop value` defaults to 1,000,000 GP and controls which NPC loot events create a confirmation prompt. Game-chat valuable/untradeable notifications are also detected independently of this threshold.

The live verification session token and pending verification ID are stored under the RuneLite `rancourclan` configuration group. They are API-issued client credentials only; no Discord, Railway, or Google secret is stored.

## User flows

### Verification

1. Select `Generate Link Code`.
2. Use `/plugin_link CODE` in Discord before expiry.
3. Select `Refresh Status`.
4. The returned API session and member profile are stored/displayed.

### Drops

The detector watches RuneLite game chat for valuable/untradeable notifications and RuneLite NPC loot events above the configured total GE-value threshold. A candidate appears on the Drops page. The player must select `Confirm Submit`; detection alone never sends data.

Each submission includes item name, source, current RSN, UTC timestamp, and detection method. Identical item/source/RSN detections are suppressed for 30 seconds.

### Staff access

The Staff page is added only after `/plugin/me` or verification status identifies the member as staff. Client-side visibility is convenience only; the API must enforce staff authorization on every staff endpoint.

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

`ApiServices` centralizes verification checks. Events, drops, and team membership require a verified profile and session. Staff operations additionally require `profile.staff=true`. The Railway API remains the final authority.

## What works without the backend

- Plugin compilation and local RuneLite launch
- All six pages and their loading/error/empty layouts
- Mock verification, announcements, events, drops, teams, and staff review
- Chat drop candidate detection, duplicate prevention, confirmation, and dismissal
- Client-side verified/staff action guards

## Backend work still required

- Railway implementation of every route in `docs/API_CONTRACT.md`
- Discord `/plugin_link` command and short-lived code exchange
- Discord announcement and event synchronization
- Secure Railway session issuing, expiry, revocation, and authorization
- Drop review persistence and Discord bot notification workflow
- Team state persistence and Discord signup synchronization
- Staff event-cache refresh endpoint
- Staff team close/lock endpoints

## Plugin Hub conventions

- Java 11 bytecode via `options.release=11`
- RuneLite client remains `compileOnly`
- Local launcher remains in `src/test`
- Plugin entry point is declared in `runelite-plugin.properties`
- Gradle `run` enables developer mode and assertions
- No bundled external-service credentials or direct Discord/Sheets clients

## License

See [LICENSE](LICENSE).
