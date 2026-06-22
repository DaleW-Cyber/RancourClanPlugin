# Early Release Checklist

## Client complete

- [x] Java 11-compatible external plugin foundation
- [x] Gradle wrapper, build, local launcher, and Plugin Hub metadata
- [x] Async typed REST client using RuneLite's OkHttp client
- [x] Configurable API base URL
- [x] Explicit opt-in mock mode with in-memory mock sessions
- [x] Verification code generation and status refresh UI
- [x] RuneLite config-backed live session persistence
- [x] Announcement loading, refresh, empty, loading, and error states
- [x] Event loading plus verified Join/Leave guards
- [x] Chat and high-value NPC-loot candidate detection with 30-second duplicate suppression
- [x] Manual drop confirmation before submission
- [x] Team Finder loading plus verified Join/Leave guards
- [x] Conditional staff page based on verified profile
- [x] Staff announcement and pending-drop review UI
- [x] Disabled/typed boundaries for staff actions missing an API route
- [x] Tests for pages, verification state, detection, and duplicate prevention

## Railway API required

- [ ] Deploy all endpoints in `API_CONTRACT.md`
- [ ] Configure TLS and production API base URL
- [ ] Implement short-lived plugin sessions, expiry, rotation, and revocation
- [ ] Enforce authenticated RSN ownership on protected actions
- [ ] Enforce staff authorization server-side
- [ ] Add rate limiting, validation, audit logging, and durable idempotency
- [ ] Decide contracts for event-cache refresh and team close/lock
- [ ] Return user-safe JSON errors consistently

## Discord bot required

- [ ] Add `/plugin_link code`
- [ ] Complete and expire link attempts safely
- [ ] Feed public/restricted announcements through the API
- [ ] Synchronize Discord events and event participation
- [ ] Send drop submissions into the existing review workflow
- [ ] Synchronize teams, signup state, and staff actions

## Release verification

- [ ] Test verification end to end against a non-production Discord guild/API
- [ ] Test expired, revoked, malformed, and unauthorized sessions
- [ ] Test API unavailable and slow-response behavior on every page
- [x] Confirm no Discord/Railway/Google credentials appear in the built JAR
- [ ] Confirm staff routes reject non-staff tokens independently of the UI
- [ ] Confirm duplicate event/team joins and drop posts are idempotent server-side
- [x] Run `./gradlew clean build`
- [x] Run `./gradlew run`
- [x] Confirm all pages render and Rancour startup logs contain no exceptions
- [ ] Complete Plugin Hub review and packaging checks
