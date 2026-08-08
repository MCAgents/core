---
name: repository-rules
description: Rules specific to the MCAgents/core repository — its current state, what exists, and what must not be assumed or introduced.
---

# Repository Rules

This is a hub. It records what is true about **this** repository and links out to
the specialized rules rather than restating them.

## Current state — read this before assuming anything

`MCAgents/core` is a **Gradle multi project build on Java 25**. The tracked files
are:

* `README.md` — project overview
* `LICENSE` — MIT, `Copyright (c) 2026 MCAgents`
* `settings.gradle`, `build.gradle`, `gradle.properties`, `gradlew`,
  `gradlew.bat`, `gradle/` — the build, the wrapper, and the version catalog
* `api/`, `common/`, `platforms/` — the ten modules, all packaged under
  `io.github.mcagents.core`
* the `.agents/` instruction tree and the `wiki/` documentation tree

The module graph, the dependency rules, and the published coordinates are
documented once, in
[`../../wiki/information/modules.md`](../../wiki/information/modules.md). Do not
restate them here; read that page rather than inferring the layout.

There is **no CI pipeline in this repository yet**. Do not describe one, do not
document commands for one, and do not write instructions that assume one.
Anything you would have to guess at is not a rule — it is a fabrication.

## What the project does

`core` holds the shared agent and API code that MCAgents' Minecraft plugins and
mods build on. Today that is one capability: driving language model agents —
OpenRouter, OpenAI, DeepSeek, and Anthropic — through
`io.github.mcagents.core.common.MCAgentsProvider`, the single public entry point.

Three constraints define the project, and a change that breaks one of them is
wrong regardless of how well it works:

* **API only.** No commands, no permissions, no listeners, no gameplay. `core` is
  consumed by plugins and mods; it is not one.
* **No memory.** Nothing stores a conversation, a cache, or per player state. A
  shared core that accumulated history would leak memory in every consumer at
  once.
* **Nothing blocks.** Every remote call returns a `CompletableFuture` and works
  off the calling thread. A tick must never wait on a network call, and on Folia
  there is no single main thread to wait in.

The `platforms/*` modules exist and compile but hold no code yet. That is scope
guidance for *where* platform code will live — never a description of code that
exists.

## Rules

* **Do not fabricate architecture.** `wiki/` describes the repository as it is.
  No speculative architecture pages, no placeholder API reference, no TODO-filled
  documents, and no page describing a module that holds no code.
* **Record commands only once they are real.** The build, test, and publish
  commands live in
  [`../../wiki/environments/setup.md`](../../wiki/environments/setup.md). Never
  write a command you have not seen work in this repository.
* **A change carries its documentation with it** — see
  [`change-propagation.md`](change-propagation.md). Code and structure changes
  update the `wiki/` pages and indexes they invalidate, in the same commit.
* **The default branch is `master`.** Branch from it, never commit to it directly —
  see [`../git/branching-strategy.md`](../git/branching-strategy.md).
* **The license is fixed.** MIT, held by MCAgents. Do not change the license, the
  copyright holder, or the year without explicit user instruction — that is a
  legal statement, not a code change.
* **Never bump the version yourself** — see [`versioning.md`](versioning.md). The
  version carrier is `project-version` in `gradle.properties`, currently
  `0.0.0`. No `wiki/logs/` version directory exists; creating one requires user
  approval, exactly like editing that property.
* **Placement is not a judgment call.** New instructions and documents go where
  [`directories.md`](directories.md) says, including creating a new folder when
  none fits.
* **Keep `README.md` and `AGENTS.md` overviews.** Detail belongs in `wiki/`;
  rules belong in `.agents/`. If detail creeps into either overview, move it down
  rather than leaving it.

## When this file goes stale

This file's "Current state" and "What the project does" sections describe the
repository at a moment in time. The moment a CI workflow lands, a `platforms/*`
module gains real code, or one of the three constraints above is deliberately
changed, they are wrong. Correcting them is part of the change that caused it —
subject to the Discovery Protocol below, propose the rewrite rather than silently
reshaping the rules.

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
