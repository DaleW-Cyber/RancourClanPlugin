# Rancour Clan Plugin

An external RuneLite plugin foundation for Rancour clan tools. Phase 1 provides a local, offline UI with placeholder verification, announcements, events, and drop submission pages. No Discord, Railway, Google Sheets, or other external service is contacted yet.

## Requirements

- Java Development Kit 11
- Git
- An internet connection for the first Gradle dependency download

The repository includes the Gradle 8.10 wrapper. A system Gradle installation is not required.

## Local development

Clone the repository and enter it:

```bash
git clone https://github.com/DaleW-Cyber/RancourClanPlugin.git
cd RancourClanPlugin
```

Build and test the plugin:

```bash
./gradlew clean build
```

On Windows PowerShell, use:

```powershell
.\gradlew.bat clean build
```

## Run RuneLite locally

Launch RuneLite in developer mode with the external plugin loaded:

```bash
./gradlew run
```

On Windows PowerShell:

```powershell
.\gradlew.bat run
```

RuneLite opens as a normal desktop application. Enable `Rancour Clan` in the configuration panel if needed, then select its `R` icon in the sidebar. The local launcher lives in `src/test` and is not included in the published plugin artifact.

## IntelliJ IDEA

1. Install a Java 11 JDK.
2. Open the repository folder in IntelliJ IDEA.
3. Import the project using the Gradle wrapper when prompted.
4. Set the project SDK and Gradle JVM to Java 11.
5. Allow Gradle synchronization to finish.
6. Run the Gradle `run` task, or run `com.rancour.clan.RancourClanPluginTest` from `src/test/java`.

Do not add the RuneLite client JAR manually. Gradle supplies the correct compile and local development classpaths.

## Project structure

```text
RancourClanPlugin/
|-- build.gradle
|-- settings.gradle
|-- runelite-plugin.properties
|-- gradlew
|-- gradlew.bat
|-- gradle/wrapper/
`-- src/
    |-- main/java/com/rancour/clan/
    |   |-- RancourClanPlugin.java
    |   |-- api/
    |   |   |-- ClanApiClient.java
    |   |   `-- RestClanApiClient.java
    |   |-- config/
    |   |   `-- RancourClanConfig.java
    |   |-- models/
    |   |   |-- Announcement.java
    |   |   |-- ClanEvent.java
    |   |   |-- DropSubmission.java
    |   |   `-- VerificationStatus.java
    |   |-- services/
    |   |   |-- AnnouncementService.java
    |   |   |-- DropService.java
    |   |   |-- EventService.java
    |   |   |-- VerificationService.java
    |   |   `-- Placeholder*Service.java
    |   `-- ui/
    |       |-- RancourClanPanel.java
    |       |-- VerificationPanel.java
    |       |-- AnnouncementsPanel.java
    |       |-- EventsPanel.java
    |       `-- DropsPanel.java
    `-- test/java/com/rancour/clan/
        `-- RancourClanPluginTest.java
```

## Architecture

`RancourClanPlugin` owns the RuneLite lifecycle and sidebar navigation. Swing components are isolated in `ui`, while domain data lives in `models`. UI pages depend on small interfaces from `services`, not on HTTP or third-party integrations.

`ClanApiClient` defines the future asynchronous REST boundary. `RestClanApiClient` currently makes no network requests and returns an explicit unsupported-operation failure if called. Phase 1 service implementations return local placeholder content. During Phase 2, API-backed service implementations can replace them without changing the page structure.

The only current configuration value is the future API base URL. It defaults to `https://api.rancourpvm.com` but is unused until REST integration begins.

## Plugin Hub conventions

- Production sources compile for Java 11.
- RuneLite is a `compileOnly` dependency and is supplied by the client at runtime.
- `runelite-plugin.properties` identifies the single plugin entry point.
- The local RuneLite launcher remains under `src/test`.
- The Gradle `run` task launches RuneLite with developer mode and assertions enabled.
- Published code must not bundle credentials, API secrets, Discord tokens, or member data.

## Phase 2 roadmap

1. Discord-based clan verification
2. Announcement feed from Discord
3. Event centre linked to Discord events
4. Automatic drop submission prompts
5. Team finder and signup system
6. Staff administration tools

Future design work will place Discord bot, Railway API, Google Sheets, drop logging, clan verification, and event participation logic behind the REST API. The RuneLite plugin should not receive direct service credentials or write to those systems itself.

## License

See [LICENSE](LICENSE).
