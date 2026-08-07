---
name: repository-rules
description: Rules specific to the MCAgents/core repository — its current state, what exists, and what must not be assumed or introduced.
---

# Repository Rules

This is a hub. It records what is true about **this** repository and links out to
the specialized rules rather than restating them.

## Current state — read this before assuming anything

As of this instruction set, `MCAgents/core` contains **no source code**. The
tracked files are:

* `README.md` — project overview
* `LICENSE` — MIT, `Copyright (c) 2026 MCAgents`
* the `.agents/` instruction tree and the `wiki/` documentation tree

There is therefore **no language, package manager, build system, test runner,
entry point, or CI pipeline in this repository yet**. Do not describe one, do not
document commands for one, and do not write instructions that assume one. Anything
you would have to guess at is not a rule — it is a fabrication.

## Intended purpose

`core` is intended to hold the shared agent and API code that MCAgents' Minecraft
plugins and mods build on. That is the project's stated direction, not its current
contents. Treat it as scope guidance for *where* code will eventually live — never
as a description of code that exists.

## Rules

* **Do not fabricate architecture.** Until source lands, `wiki/` describes the
  repository as it is. No speculative architecture pages, no placeholder API
  reference, no TODO-filled documents.
* **Record commands only once they are real.** When a build system is introduced,
  add its actual install / build / test / run commands to
  [`../../wiki/environments/setup.md`](../../wiki/environments/setup.md) and
  summarize them here. Never write a command you have not seen work in this repo.
* **The default branch is `master`.** Branch from it, never commit to it directly —
  see [`../git/branching-strategy.md`](../git/branching-strategy.md).
* **The license is fixed.** MIT, held by MCAgents. Do not change the license, the
  copyright holder, or the year without explicit user instruction — that is a
  legal statement, not a code change.
* **Never bump the version yourself** — see [`versioning.md`](versioning.md).
  This repository has no version carrier and no `wiki/logs/` version directory
  yet; creating either one requires user approval.
* **Placement is not a judgment call.** New instructions and documents go where
  [`directories.md`](directories.md) says, including creating a new folder when
  none fits.
* **Keep `README.md` and `AGENTS.md` overviews.** Detail belongs in `wiki/`;
  rules belong in `.agents/`. If detail creeps into either overview, move it down
  rather than leaving it.

## When this file goes stale

The moment real source, a manifest, or a CI workflow lands in this repository,
this file's "Current state" section is wrong. Updating it is part of the commit
that introduces them — subject to the Discovery Protocol below, propose the
rewrite rather than silently reshaping the rules.

## Discovery Protocol

While working, if you notice an instruction worth adding — a new rule, or new
content for an existing instruction file — do NOT create or edit it yourself.
Collect the findings, and when the task is done present them to the user:

* one finding per message block, each in its own code block;
* include the proposed file path, `name`, `description`, and the full proposed
  body;
* explain in one line why it is worth adding.

Then let the user select which findings to apply. Create only the selected ones.
Never batch-apply, never apply silently.
