# 0.4.0

Released: 2026-08-08

The request timeout becomes configurable, and stays the only one in MCAgents.

## Upgrading

Nothing is required. A `config.yml` written by an earlier version has no
`request_timeout_seconds` key, and a missing key keeps the previous behaviour of
60 seconds.

## Added

- **`request_timeout_seconds` in `config.yml`.** How long one request may take
  before it is abandoned. Allowed range 5 to 600; anything outside falls back to
  60 with a warning rather than refusing to start.
- A `request timeout` line in `/agents` status.
- A `ModTokenCommands` constructor taking a `Duration`, so the mod side is
  configurable the same way.

## Changed

- `reloadCredentials()` re-registers each vendor rather than only reloading the
  pool, because the timeout travels on the credential template. Reloading the
  pool alone would have kept the old value silently.

## Why the timeout lives here

MCAgents plugins have no timeout setting, pass none, and cannot override this
one. That is deliberate: because a request always answers or fails inside this
window, a consumer can hold a "waiting" state — refusing a second request until
the first finishes — without owning a timer of its own. Two timeouts would be two
places to get wrong, and the consumer's would have to duplicate this one to be
correct.

A timeout is also **not** a credential failure. Nothing is evicted and nothing is
retried: a model that has not answered in this long is unlikely to answer sooner
on a second attempt, and retrying doubles the bill.

## Unchanged

- The **connect** timeout stays a fixed 15 seconds. It bounds opening a socket
  rather than waiting for a model, and exposing it would be a knob nobody has a
  reason to turn.
