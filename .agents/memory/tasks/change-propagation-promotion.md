---
name: memory-tasks-change-propagation-promotion
description: Deleting the local change-propagation rule once the shared set grew its own — the shadowing problem, the merge gate, and what replaced it.
---

# Task: Drop the local change-propagation rule

**Status:** branch pushed, blocked on the shared release — must not merge yet

## 2026-08-12

**What happened.** The shared set gained `rules/change-propagation.md` in its `0.3.0`
release. This repository has carried a local rule of the same `name` since the original
instruction generation, and two earlier task records
([`agents-setup.md`](agents-setup.md), [`agents-instruction-rewrite.md`](agents-instruction-rewrite.md))
both note that it was *kept local although its content was arguably universal*. That
question is now settled upstream, so the local copy is deleted.

**Why deleting it is not optional.** Local overrides shared **by `name`, whole-file**.
Two files named `change-propagation` means the local one wins and the shared one is never
read — so every future improvement upstream lands nowhere here, silently. This is the
duplicate-instruction problem the audit exists to find, and keeping the copy would have
made this repository's rule drift from the one every other repository follows.

**Not an override.** An override is a deliberate, registered replacement with a row in
`root-index.md`. This was never that: it predates the shared rule entirely. The override
table stays empty — nothing here overrides the shared set.

**What replaced it.** The trigger row in `AGENTS.md` now reads
`Change code or structure that a document describes` →
`{shared}/rules/change-propagation.md`, worded exactly as the shared
`rules/auto-activation.md` row it mirrors. It sits in the `{shared}` block, not among the
local rows at the bottom, which are now three: `knowledge/minecraft-platform.md`,
`wiki/context/repository-map.md`, `rules/repository.md`.

**What the shared rule drops, deliberately.** The local file carried a table mapping
changes to specific pages — `wiki/information/modules.md`, `wiki/environments/setup.md`,
`wiki/reference/`, `wiki/guides/`. The shared rule cannot name those, because it is read
by repositories with no such pages; it asks *would a reader who trusted this page now be
wrong?* instead. The obligation is the same, resolved per repository rather than listed.

**Also fixed.** `rules/repository.md` linked the deleted file by relative path. It now
points at `{shared}/rules/change-propagation.md`, matching how that file cites every
other shared rule.

**No version change.** `project-version` stays `0.4.0` and no new `wiki/logs/` directory
was created. Nothing shipped here changed; a local rule was removed because the shared
set now carries it.

**Merge gate — this branch must not merge yet.** This repository resolves the shared set
through the connector, which serves the **deployed** version, not a branch. Until
`LXAgents/mcp-server` `0.3.0` is merged *and deployed*, `agents://rules/change-propagation.md`
does not resolve. Merging this branch before then leaves the repository with no
change-propagation rule at all — the local one gone and the shared one not yet reachable.
Order: merge `0.3.0` upstream, deploy it, confirm the resource resolves, then merge here.
