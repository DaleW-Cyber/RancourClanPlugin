# RuneLite Sidebar UI QA

Use the normal RuneLite sidebar width. Do not drag the sidebar wider to make this check pass.

1. Run `./gradlew run`, enable Rancour Clan, and open the `R` sidebar icon.
2. Check Verification, News, Events, Drops, Teams, and Staff.
3. Confirm long titles, descriptions, role lists, account names, status/error messages, and form labels wrap inside each card.
4. Confirm timestamps use the compact `29 Jun 2026 16:50` form.
5. Confirm action buttons are stacked where two full labels would otherwise be cramped.
6. Verify a profile shows separate Discord, Active RSN, Linked RSNs, Clan rank, Staff, Expires, and Last checked rows.
7. Log into a linked alt and confirm drop submission is enabled.
8. Log into an unlinked test account and confirm both Verification and Drops show the warning and Confirm Submit is disabled.
9. Capture screenshots of all six pages at the normal width for release review. No text should cross the right edge or create a horizontal scrollbar.
