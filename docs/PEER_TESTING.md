# Rancour PvM RuneLite Plugin Test Setup

This guide is for members testing the Rancour PvM plugin before it is available in the RuneLite Plugin Hub.

## What You Need

- RuneLite installed
- Java 11 or newer installed
- The Rancour test jar from staff:
  - `rancour-clan-plugin-1.3.0-all.jar`
- The launcher file from staff:
  - `launch-rancour-plugin.bat`

Do not copy the jar into `.runelite\externalplugins`. RuneLite will not load this test jar from there.

## Step 1: Install Java

1. Open PowerShell.
2. Type:

```powershell
java -version
```

3. If it shows Java 11 or newer, continue to Step 2.
4. If PowerShell says Java is not recognised, install Java:

```text
https://adoptium.net/
```

Choose Eclipse Temurin Java 17 for Windows.

## Step 2: Make A Test Folder

Create a folder anywhere easy, for example:

```text
Desktop\Rancour Plugin Test
```

Put both files in that folder:

```text
Rancour Plugin Test
|-- rancour-clan-plugin-1.3.0-all.jar
`-- launch-rancour-plugin.bat
```

## Step 3: Start The Plugin

Double-click:

```text
launch-rancour-plugin.bat
```

RuneLite should open with the Rancour PvM test plugin loaded.

If Windows shows a security warning, choose:

```text
More info -> Run anyway
```

Only do this for the launcher file provided by Rancour staff.

## Step 4: Enable The Plugin

In RuneLite:

1. Open the settings panel.
2. Search for:

```text
Rancour
```

3. Enable the plugin if it is not already enabled.
4. Open the Rancour sidebar icon.

## Step 5: Link Your Discord

1. Open the Rancour plugin panel in RuneLite.
2. Click `Generate Link Code`.
3. Copy the code.
4. Go to Discord.
5. Use the Rancour verification panel or `/plugin_link <code>`.
6. Go back to RuneLite.
7. Click `Refresh Status`.

Once linked, the plugin should show News, Events, Teams, and any enabled clan tools.

## If It Does Not Open

Try these checks:

1. Make sure both files are in the same folder.
2. Make sure the jar is named like:

```text
rancour-clan-plugin-1.3.0-all.jar
```

3. Open PowerShell in the folder and run:

```powershell
java -ea -jar .\rancour-clan-plugin-1.3.0-all.jar --developer-mode --debug
```

4. If an error appears, screenshot the PowerShell window and send it to staff.

## Common Mistakes

- Do not use the normal RuneLite/Jagex Launcher shortcut for this test version.
- Do not copy the jar into `.runelite\externalplugins`.
- Do not use `rancour-clan-plugin-1.3.0-sideload.jar` unless staff specifically asks you to.
- Use the `-all.jar` file for peer testing.

## How To Stop Testing

Close RuneLite.

Nothing was installed into RuneLite permanently. The test plugin only loads when you start RuneLite using the Rancour launcher or the PowerShell command above.
