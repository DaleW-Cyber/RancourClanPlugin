# Rancour RuneLite API Contract

This document defines the early-release contract between the RuneLite plugin and the Railway-hosted Rancour API. The API, not the client, must authenticate members, enforce staff access, validate identifiers, rate-limit actions, and communicate with Discord or Google-backed services.

## General rules

- Base URL is configured in RuneLite; all routes below are relative to it.
- Request and response bodies use `application/json`.
- Times use ISO-8601 UTC strings, for example `2026-06-22T18:30:00Z`.
- Authenticated requests use `Authorization: Bearer <sessionToken>`.
- Public announcement/event/team reads may omit authorization. If a token is supplied, the API filters member-specific content server-side.
- IDs are opaque strings.
- Success responses must contain JSON; the client treats an empty body as an error.
- Error responses should use `{ "message": "User-friendly explanation" }` with an appropriate HTTP status.
- Session tokens must be short-lived, revocable, scoped to the plugin, and must never be Discord bot tokens.

## Health

### `GET /health`

Unauthenticated connectivity check used by the Verification page's **Test API Connection** button.

Response:

```json
{
  "status": "ok"
}
```

## Verification

### `POST /plugin/verification/start`

Starts a short-lived link attempt. No authentication required.

Response:

```json
{
  "code": "ABCD-1234",
  "verificationId": "opaque-attempt-id",
  "expiresAt": "2026-06-22T18:40:00Z"
}
```

The Discord bot must accept `/plugin_link ABCD-1234`, bind the attempt to the Discord user, and mark it complete.

### `GET /plugin/verification/status`

The plugin sends `verificationId` as a query parameter while linking. When refreshing an existing session it sends the bearer token.

Response:

```json
{
  "state": "verified",
  "sessionToken": "short-lived-plugin-session",
  "profile": {
    "discordName": "Display Name",
    "rsn": "RuneScape Name",
    "primaryRsn": "RuneScape Name",
    "linkedRsns": ["RuneScape Name", "Approved Alt"],
    "clanRank": "Member",
    "staff": false,
    "expiresAt": "2026-06-29T18:30:00Z",
    "lastCheckedAt": "2026-06-22T18:30:00Z"
  },
  "expiresAt": "2026-06-29T18:30:00Z",
  "lastCheckedAt": "2026-06-22T18:30:00Z"
}
```

Valid states are `pending`, `verified`, `expired`, and `revoked`. `sessionToken` is returned only when a link completes or rotates. `profile` may be omitted; the client then calls `/plugin/me`.

### `GET /plugin/me`

Requires authentication. Returns the profile object shown above. The server should return `401` for expired/revoked sessions and `403` where clan access is not permitted.

## Plugin settings

### `GET /plugin/settings`

Returns public client settings and the approved Rancour drop catalogue. The catalogue is sourced from the Discord bot `drop_catalog.py`, which also powers manual Discord drop submission and ADS parsing. The catalogue is used by RuneLite to suppress unknown high-value loot before it becomes a pending drop candidate; the API must still validate the submitted item again.

```json
{
  "dropsPanelEnabled": true,
  "approvedDrops": ["Twisted bow", "Dexterous prayer scroll"],
  "approvedDropSources": [
    {
      "boss": "Chambers of Xeric",
      "drops": ["Twisted bow", "Dexterous prayer scroll"]
    }
  ]
}
```

## Announcements

### `GET /plugin/announcements`

Returns a JSON array. Public items are available without verification. Restricted items require a valid bearer token and also carry `restricted=true`; the client hides restricted items when not verified.

```json
[
  {
    "id": "announcement-id",
    "title": "Clan update",
    "message": "Message body",
    "priority": "high",
    "createdAt": "2026-06-22T18:30:00Z",
    "expiresAt": "2026-06-23T18:30:00Z",
    "author": "Staff Name",
    "restricted": false
  }
]
```

RuneLite announcement cards render only `title` and `message`. The remaining fields stay in the model for API filtering, chat-notification priority checks, and staff tools.

## Events

### `GET /plugin/events`

Returns:

```json
[
  {
    "id": "event-id",
    "name": "Clan PvM Night",
    "startTime": "2026-06-22T20:00:00Z",
    "description": "Event details",
    "host": "Mutable",
    "hostRsn": "Mutable",
    "hostDisplayName": "Dale",
    "status": "open",
    "signupCount": 8,
    "joined": false,
    "visibility": "restricted",
    "requiredRoleIds": ["123456789012345678"],
    "sourceChannelId": "234567890123456789"
  }
]
```

### `POST /plugin/events/{id}/join`
### `POST /plugin/events/{id}/leave`

Require authentication. Body is `{}`. Return:

```json
{ "success": true, "message": "Event signup updated" }
```

`visibility` is one of `public`, `member`, `staff`, or `restricted`. Anonymous callers receive only public events. Verified callers receive member events; staff events require API-derived staff status; restricted events require at least one `requiredRoleIds` match against the Discord roles stored during verification. `sourceChannelId` may be null for external Discord events. RuneLite labels staff events as `STAFF EVENT` and visible restricted events as `RESTRICTED`, but it does not display role IDs or permission internals.

The API owns Discord event participation synchronization and idempotency. Join and Leave must enforce the same visibility rule as the list endpoint. A denied action returns HTTP `403` with:

```json
{ "message": "You do not have access to this event" }
```

RuneLite Events are currently read-only. Joining/leaving events is handled in Discord.

## Drops

### `POST /plugin/drops`

Requires authentication and explicit player confirmation in RuneLite.

```json
{
  "itemName": "Twisted bow",
  "source": "Game chat",
  "rsn": "RuneScape Name",
  "timestamp": "2026-06-22T18:30:00Z",
  "detectionMethod": "chat_message"
}
```

Response:

```json
{
  "id": "submission-id",
  "status": "pending",
  "message": "Drop submitted for review"
}
```

The API must verify the session/RSN relationship, validate fields, and perform durable duplicate protection in addition to the client's short local window.

`rsn` is a compatibility alias for `primaryRsn`. `linkedRsns` is the complete active, Discord-side verified account set. The API accepts a drop only when its `rsn` matches one of these names after OSRS normalization. An unknown account returns:

```json
{ "message": "This RuneLite account is not linked to your Discord profile." }
```

RuneLite must not create or modify linked accounts. The authenticated Discord bot owns the internal sync contract used by future staff-approved alt management.

Drop submissions can be globally disabled with `dropsPanelEnabled=false`. When disabled, the API rejects `POST /plugin/drops` with:

```json
{ "message": "Drop submissions are currently disabled." }
```

The API also rejects item names that are not present in the approved Rancour drop catalogue with:

```json
{ "message": "This item is not part of the approved Rancour drop catalogue." }
```

Discord does not need to recognise a drop before RuneLite sees it; RuneLite detects the drop and the API validates it against the catalogue.
Plugin-submitted drops are stored with `origin=plugin`, allowing Discord ADS to skip matching RSN + item + source detections within its configured dedupe window.

## Teams

### `GET /plugin/teams`

```json
[
  {
    "id": "team-id",
    "activity": "Theatre of Blood",
    "host": "Host Name",
    "requiredRoles": ["Freeze", "Melee"],
    "currentMembers": 3,
    "capacity": 5,
    "world": 416,
    "voiceRequired": true,
    "status": "open",
    "staffHosted": true,
    "tags": ["learner", "staff-hosted"],
    "joined": false,
    "joinedMembers": ["Mutable", "Slow"],
    "createdAt": "2026-06-22T18:30:00Z",
    "expiresAt": "2026-06-22T20:30:00Z",
    "fullAt": null,
    "closedAt": null
  }
]
```

### `POST /plugin/teams/{id}/join`
### `POST /plugin/teams/{id}/leave`
### `POST /plugin/teams/{id}/close`

Require authentication. Body is `{}`. Join and Leave return an `ActionResult` as used by event signup. The API must enforce capacity, expiry, and role/signup rules. Close must be limited to the team host or staff. Expired teams return `This team has expired.` Full teams return `This team is full.`

### `POST /plugin/teams`

Requires authentication.

```json
{
  "activity": "Theatre of Blood",
  "capacity": 5,
  "world": 416,
  "voiceRequired": true,
  "notes": "Learner friendly",
  "activeRsn": "Mutable",
  "requiredRoles": ["Freeze", "Melee"],
  "tags": ["learner"],
  "staffHosted": false
}
```

The API records the authenticated profile as host and adds the creator as a joined member. RuneLite sends the active logged-in RSN as `activeRsn`; the API accepts it only when it is in the verified linked-RSN set. Discord-created teams use the primary RSN. Teams display members using RSN as the primary alias, falling back to Discord display name only if no RSN is available. Staff-hosted teams require API-derived staff status. Teams expire two hours after creation. Full teams set `fullAt`, queue one ready notification, remain visible for five minutes, then disappear from normal Team Finder responses. If a member leaves before the grace window ends and the team is no longer full, `fullAt` and pending ready-notification state are cleared. RuneLite shows a one-time local chatbox notice for joined teams that return as full.

### `GET /plugin/staff/teams`
### `PATCH /plugin/staff/teams/{id}`
### `DELETE /plugin/staff/teams/{id}`

Require staff authentication. Staff team cards use `GET /plugin/staff/teams`. `PATCH` can update `activity`, `capacity`, `world`, `voiceRequired`, `requiredRoles`, `tags`, and `status`. Capacity cannot be set below the current joined member count. `DELETE` closes the team and preserves audit history.

Discord bot Team Finder actions use internal API routes with `X-Rancour-Bot-Token` and `discordUserId`. They do not use RuneLite bearer sessions. An unlinked Discord user should receive:

```json
{ "message": "Link your RuneLite account first with /plugin_link." }
```

## Staff drop review

All staff routes require an authenticated profile with server-side staff authorization.

### `GET /plugin/staff/drop-submissions`

Returns pending submissions:

```json
[
  {
    "id": "submission-id",
    "itemName": "Twisted bow",
    "source": "Game chat",
    "rsn": "RuneScape Name",
    "submittedAt": "2026-06-22T18:30:00Z",
    "status": "pending"
  }
]
```

### `POST /plugin/staff/drop-submissions/{id}/approve`
### `POST /plugin/staff/drop-submissions/{id}/reject`

Body is `{}`. Return an `ActionResult`. Decisions should be idempotent and audit logged.

## Staff announcements

### `POST /plugin/staff/announcements`

```json
{
  "title": "Clan update",
  "message": "Message body",
  "priority": "normal",
  "expiresAt": "2026-06-23T18:30:00Z"
}
```

Return the created `Announcement` model. The API owns Discord publication and persistence.

`expiresAt` must be in the future and no more than seven days after the API server's current UTC time. RuneLite uses a dropdown with 1 hour, 6 hours, 12 hours, 1 day, 2 days, 3 days, and 7 days options instead of manual date entry.

### `DELETE /plugin/staff/announcements/{announcementId}`

Requires staff authentication. Deletes or hides the announcement from future `GET /plugin/announcements` responses. Return:

```json
{ "success": true, "message": "Announcement deleted" }
```

## Staff settings

### `POST /plugin/staff/settings/drops-panel`

Requires staff authentication. Body:

```json
{ "enabled": true }
```

Returns the same model as `GET /plugin/settings`. The API must enforce staff authorization server-side; RuneLite confirmation dialogs are only a usability guard.

## Missing backend contracts

The client contains typed methods and disabled/clear UI states for these requested staff capabilities, but no URL is invented because the supplied contract does not define one:

- Refresh Discord event cache
- Lock a team

Before enabling them, agree endpoint paths, authorization, request bodies, response models, audit rules, and idempotency. Suggested routes must be approved on the Railway/Discord side rather than assumed by the RuneLite client.
