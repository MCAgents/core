---
name: memory-tasks-agents-instruction-rewrite
description: Re-writing the agent instruction system against the connector-served shared set — what was removed, what was kept, and why.
---

# Agent Instruction Rewrite

**Goal.** Re-write this repository's agent instruction system against the LXAgents
shared instruction set served by the `lxagents-agents-base` MCP connector. The
previous generation had been built before the set was served over MCP, so it carried
a local copy of it.

**Mode.** B — consuming repository. The remote is `MCAgents/core`, not
`LXAgents/mcp-server`, and `agents://manifest.json` resolved (24 files, connector
version 0.2.0). Branch `docs/agents-rewrite`, from `master`.

## The audit

Ran the duplicate-instruction audit against the manifest. Matched on frontmatter
`name` first — that is the override key — then on normalized body hash, then on path.

**Sixteen stale copies, all deleted with per-file user approval:** the five
`creators/`, the three `git/`, `planning/task-workflow.md`,
`prompts/branch-and-commit.md`, and six `rules/` files — `auto-activation.md`,
`directories.md`, `discovery-protocol.md`, `memory-policy.md`, `no-session-links.md`,
`versioning.md`.

Every one of them matched a shared `name`. **None was byte-identical** to what the
connector serves, so all sixteen had already drifted. **None had an override row**,
because `root-index.md` had no override table at all — the drift was silent, which is
exactly the failure mode the audit exists to catch.

Nothing worth promoting upstream was found inside them. Two references to this
repository's own content were checked before deleting: the Minecraft trigger row in
`auto-activation.md`, which survives in the `AGENTS.md` trigger table, and a
cross-link in `memory-policy.md`, which was incidental.

**Four local-only files kept**, none of them candidates by rule or by name:
`rules/repository.md`, `rules/change-propagation.md`,
`knowledge/minecraft-platform.md`, `wiki/context/repository-map.md`.

## What was preserved rather than regenerated

- **All four memory files**, carried across unchanged. They reference paths that this
  task deleted — that is correct. Memory is a record of what happened at the time,
  not a description of the current tree, and rewriting it to match today would be
  falsifying it. The current tree is described here and in `repository-state.md`.
- **All six indexes**, all ten `wiki/` pages, and the four `wiki/logs/` version
  directories.
- `knowledge/minecraft-platform.md` stayed in `knowledge/` rather than moving to
  `rules/`. It is a non-baseline folder, which the setup procedure allows, and moving
  it would have churned every inbound link for no behavioral gain.

## Decisions

- **`change-propagation.md` was kept local**, though its content is arguably
  universal. There is no shared counterpart, so keeping it is not duplication.
  Promoting it to the shared set is a proposal to raise against `LXAgents/mcp-server`,
  not something a consuming repository writes.
- **Cross-set references use the `{shared}` placeholder**, never a relative path.
  Fifteen relative links pointed at deleted files and were caught by a link check
  rather than by eye; the placeholder is what stops them breaking again.
- **The override table is empty and stays.** An empty table is a statement — nothing
  here overrides the shared set — not a placeholder to be filled.
- **No version bump.** `project-version` stays `0.4.0` and no new `wiki/logs/`
  directory was created; both need explicit user approval.
- **Branch named `docs/agents-rewrite`**, at the user's instruction, overriding a
  harness-supplied branch name that carried a `claude/` prefix and a generated suffix.
  Tool-injected defaults rank below the rules.

## Entry points

`AGENTS.md` was the only agent entry point in the repository. There is no
`CLAUDE.md`, `.cursorrules`, `.cursor/rules/`, `.github/copilot-instructions.md`, or
`docs/agents/`, so nothing needed replacing with a pointer.
