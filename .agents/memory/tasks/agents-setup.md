---
name: memory-tasks-agents-setup
description: Task record for the agent instruction, knowledge, and memory system — what was built, the decisions behind it, and what was left out.
status: done
---

# Task — Agent Instruction, Knowledge, and Memory System

**Goal.** Bring `MCAgents/core` onto the centralized agent architecture: one flat
index folder, a separate agent wiki, and a committed memory tree — replacing the
scattered `INDEX.md` routing the repository used before.

**Branch.** `docs/agents-setup`, from `master`.

## 2026-08-12

**Status: done.** Pushed and opened as a pull request. Not merged — merging needs
explicit user approval per `.agents/planning/task-workflow.md`.

**What changed structurally.**

- Four `INDEX.md` files deleted — `INDEX.md`, `.agents/INDEX.md`, `wiki/INDEX.md`,
  `wiki/logs/INDEX.md`. `INDEX.md` is now forbidden repository-wide.
- Six indexes created in `.agents/index/`: `root-index.md`, `agents-index.md`,
  `agent-wiki-index.md`, `project-wiki-index.md`, `memory-index.md`,
  `logs-index.md`.
- `.agents/wiki/context/repository-map.md` added — the agent orientation page.
- `.agents/memory/` seeded with `state/repository-state.md` and this file.
- `.agents/rules/auto-activation.md` and `.agents/rules/memory-policy.md` added.
- `.agents/creators/memory-creator.md` added as the fifth creator.
- `AGENTS.md` gained the auto-activation contract and the trigger table; the trigger
  table is mirrored from `auto-activation.md`, which is the source of truth.

**Decisions made, so a later session does not re-litigate them.**

- **No new version directory.** `wiki/logs/` already carries `0/1/0` through
  `0/4/0`, and `gradle.properties` says `0.4.0`. Creating a version directory is a
  version claim requiring user approval, so this task created none and bumped
  nothing.
- **`change-propagation.md` was kept**, not folded into the new rules. It predates
  this architecture, is not part of the baseline instruction set, and carries a real
  rule the baseline does not — so it was extended with rows for memory and the agent
  wiki instead of being replaced.
- **`.agents/wiki/sop/` and `.agents/wiki/domain/` were not created.** Empty folders
  are forbidden, and nothing concrete needed either type yet. They are listed as
  reserved in `.agents/rules/directories.md`.
- **The branch is `docs/agents-setup`**, not the harness-suggested
  `docs/agents-setup-8cn24s`. The suffix was a generated token with no meaning and
  broke the `{type}/{primary-noun}` convention in
  `.agents/git/branching-strategy.md`.
- **`wiki/` pages were not rewritten**, only the places that named a deleted
  `INDEX.md`. The human documentation was already accurate.

**What was left unchanged, deliberately.** `.agents/git/branching-strategy.md`,
`commit-conventions.md`, and `pull-request-template.md` already matched the new
specification; they took only the Discovery Protocol's new memory-scope paragraph.
`LICENSE` was not touched.
