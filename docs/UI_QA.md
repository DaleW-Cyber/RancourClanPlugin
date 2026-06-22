# RuneLite Sidebar UI QA

Use the normal RuneLite sidebar width. Do not drag the sidebar wider to make this check pass.

1. Run `./gradlew run`, enable Rancour Clan, and open the `R` sidebar icon.
2. Check Verification, News, Events, Drops, Teams, and Staff.
3. Confirm long titles, descriptions, role lists, account names, status/error messages, and form labels wrap inside each card.
4. Confirm timestamps use the compact `29 Jun 16:50` form.
5. Confirm action buttons are stacked where two full labels would otherwise be cramped.
6. Verify a profile shows separate Discord, Active RSN, Linked RSNs, Clan rank, Staff, Expires, and Last checked rows.
7. Confirm News cards show only title and body with visible borders/separators.
8. Confirm Events cards show only title/name, start date/time, and description; no Join, Leave, Host, Status, or Signup Count fields should be visible.
9. Log into a linked alt and confirm drop submission is enabled when Staff -> Drops Panel is enabled.
10. Detect or mock an approved catalogue drop and confirm it appears in Drops. Detect or mock an unknown high-value item and confirm it does not appear.
11. Disable Staff -> Drops Panel with confirmation and confirm the Drops tab hides or shows `Drop submissions are currently disabled.`
12. Log into an unlinked test account and confirm both Verification and Drops show the warning and Confirm Submit is disabled.
13. Open Staff. Confirm the main Staff menu fits without scrolling and shows only working tools.
14. Open Staff -> Announcements. Confirm Title, Body, Priority, Expiry, Create, current-announcement Delete buttons, status, and Back fit without clipped text.
15. Confirm Staff does not show disabled placeholders for Pending Drops, Event Cache, Close Team, or Lock Team. Drop approvals remain in Discord for now.
16. Capture screenshots of all pages at the normal width for release review. No text should cross the right edge or create a horizontal scrollbar.
