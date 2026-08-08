# Overview

`core` is the shared foundation repository for **MCAgents**. It is intended to
hold the agent and API code that MCAgents' Minecraft plugins and mods build on, so
that behavior common to several projects lives in one place instead of being
copied between them.

What that means today is one capability: driving language model agents —
OpenRouter, OpenAI, DeepSeek, and Anthropic — from a plugin or a mod, through a
single class. The core is API only. It has no commands, no permissions, and no
memory: it stores no conversation and no per player state, so a consumer keeps
whatever history it wants and replays it.

## Current state

The `api` and `common` modules carry the agent API and its implementation. The
eight `platforms/*` modules exist, compile, and produce a jar, but hold only
their `package-info.java` so far. What exists today is:

| Path | What it is |
|---|---|
| `README.md` | Project overview and quick start. |
| `AGENTS.md` | Entry point for agents: reading order, routing, standing conventions. |
| `INDEX.md` | Root router pointing at every index in the repository. |
| `LICENSE` | MIT license, `Copyright (c) 2026 MCAgents`. |
| `settings.gradle`, `build.gradle`, `gradle.properties` | The Gradle multi project build configuration. |
| `gradlew`, `gradlew.bat`, `gradle/` | The Gradle wrapper and the version catalog. |
| `api/`, `common/`, `platforms/` | The ten build modules — see [`modules.md`](modules.md). |
| `.agents/` | The agent instruction set — rules, git conventions, planning, platform knowledge, prompts, creators. |
| `wiki/` | This documentation tree. |

There is no CI pipeline in the repository at this point. Build, test, and publish
commands are on [`../environments/setup.md`](../environments/setup.md).

## How the repository is organized

The project separates three kinds of content, and keeps them strictly apart:

* **Instructions** — rules an agent follows — live under `.agents/`, one topic per
  file, at `.agents/{folder}/{file}.md`.
* **Documentation** — pages a human reads — live under `wiki/`, at
  `wiki/{folder}/{file-name}.md`. This page is one of them.
* **Overviews** — `README.md` and `AGENTS.md` — stay short and point elsewhere.
  Detail is never left in them; it is moved down into `wiki/`.

Navigation runs through a tree of `INDEX.md` files: the root `INDEX.md` lists the
indexes, each index lists what its own scope contains. The point is that an agent
can find the one file it needs by reading a few small tables, rather than loading
the whole repository. Start at [`INDEX.md`](../../INDEX.md).

## Change logs

Released versions are recorded under `wiki/logs/{Major}/{Minor}/{Patch}/`. No
version has been declared yet, so no version directory exists. See
[`../logs/INDEX.md`](../logs/INDEX.md).
