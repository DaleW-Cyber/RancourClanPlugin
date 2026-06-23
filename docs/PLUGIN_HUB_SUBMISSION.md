# Plugin Hub Submission Checklist

Use this checklist when submitting `Rancour PvM` to the RuneLite Plugin Hub.

## Plugin Repository

- Repository: `https://github.com/DaleW-Cyber/RancourClanPlugin.git`
- Main class: `com.rancour.clan.RancourClanPlugin`
- Plugin Hub display name: `Rancour PvM`
- Build type: `standard`
- Java target: 11
- License: BSD 2-Clause
- Public config does not expose mock mode, API base URL, tokens, or secrets.

Before submitting:

```powershell
.\gradlew.bat clean build
```

Push the final plugin commit to GitHub and copy the full 40-character commit hash.

## Plugin Hub Marker

Fork `https://github.com/runelite/plugin-hub`, then add a file:

```text
plugins/rancour-pvm
```

Contents:

```properties
repository=https://github.com/DaleW-Cyber/RancourClanPlugin.git
commit=<full 40-character plugin commit hash>
```

The Plugin Hub pull request should contain only that marker file. If review feedback requires plugin repo changes, push those changes to `RancourClanPlugin` first, then update the marker `commit=` hash in the same Plugin Hub pull request.

## Review Notes

- The plugin is client-only and communicates only with the configured production Rancour API.
- RuneLite does not contain Discord bot tokens, Google credentials, Railway secrets, or database URLs.
- Events are read-only in RuneLite; Discord remains the event participation workflow.
- Drops are filtered against the approved catalogue returned by the API.
