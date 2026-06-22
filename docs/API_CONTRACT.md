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
    "host": "Host Name",
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

`visibility` is one of `public`, `member`, `staff`, or `restricted`. Anonymous callers receive only public events. Verified callers receive member events; staff events require API-derived staff status; restricted events require at least one `requiredRoleIds` match against the Discord roles stored during verification. `sourceChannelId` may be null for external Discord events.

The API owns Discord event participation synchronization and idempotency. Join and Leave must enforce the same visibility rule as the list endpoint. A denied action returns HTTP `403` with:

```json
{ "message": "You do not have access to this event" }
```

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
    "joined": false
  }
]
```

### `POST /plugin/teams/{id}/join`
### `POST /plugin/teams/{id}/leave`

Require authentication. Body is `{}`. Return an `ActionResult` as used by event signup. The API must enforce capacity and role/signup rules.

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

## Missing backend contracts

The client contains typed methods and disabled/clear UI states for these requested staff capabilities, but no URL is invented because the supplied contract does not define one:

- Refresh Discord event cache
- Close a team
- Lock a team

Before enabling them, agree endpoint paths, authorization, request bodies, response models, audit rules, and idempotency. Suggested routes must be approved on the Railway/Discord side rather than assumed by the RuneLite client.
