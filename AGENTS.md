---
name: agents-entry-point
description: Entry point for agents working in MCAgents/core — the auto-activation contract, the trigger table, reading order, and routing protocol.
---

# AGENTS

`MCAgents/core` holds the shared agent and API code that MCAgents' Minecraft plugins
and mods build on: a Gradle multi project on Java 25, whose `api` and `common`
modules carry the language model API and whose `platforms/*` modules cover the three
Bukkit servers and the two mod loaders. See
[`wiki/information/overview.md`](wiki/information/overview.md).

This file is an **entry point and an activation contract**. It contains no rules of
its own; it tells you which rules apply and where they are.

## Auto-Activation

The instruction set in `.agents/` is **always active**. It applies to every task in
this repository whether or not the user mentions it, links to it, or asks for it.
Treat these files as standing orders, not as optional reference material.

At the start of every session, before doing any work:

1. Read `AGENTS.md` (this file).
2. Read [`.agents/index/root-index.md`](.agents/index/root-index.md).
3. Read [`.agents/index/memory-index.md`](.agents/index/memory-index.md) and load
   only the memory rows whose scope matches the current request, so you continue
   prior work instead of restarting it.
4. Match the request against the trigger table below and load the instruction files
   it names.

If a rule in `.agents/` conflicts with a habit, a default, or a template you would
otherwise follow, the rule in `.agents/` wins. If it conflicts with an explicit
instruction from the user in this session, the user wins — and you say out loud which
rule you are setting aside.

## Trigger table

| When you are about to… | Load and obey |
|---|---|
| Take in any new request of more than one step | `{shared}/planning/task-workflow.md` |
| Start work on a confirmed task | `{shared}/prompts/branch-and-commit.md` |
| Create a branch | `{shared}/git/branching-strategy.md` |
| Write a commit message | `{shared}/git/commit-conventions.md` |
| Write **any** commit, tag, PR, comment, or file that will be committed or posted | `{shared}/rules/no-session-links.md` |
| Open or update a pull request | `{shared}/git/pull-request-template.md` |
| Decide where a new file goes | `{shared}/rules/directories.md` |
| Wonder whether something is local or shared, or need to override a shared rule | `{shared}/rules/shared-instructions.md` |
| Resolve, connect, or fail to reach the shared set | `{shared}/rules/mcp-connector.md` |
| Add, move, rename, or delete any file under `.agents/` or `wiki/` | `{shared}/creators/index-creator.md` |
| Write a rule or instruction | `{shared}/creators/instruction-creator.md` |
| Notice a rule worth adding, or find an instruction that is wrong | `{shared}/rules/discovery-protocol.md` |
| Write documentation, an SOP, or a domain guideline | `{shared}/creators/information-creator.md` |
| Record progress, a decision, or session state | `{shared}/creators/memory-creator.md` |
| Decide what may be written to memory, and how | `{shared}/rules/memory-policy.md` |
| Change code or structure that a document or index describes | [`.agents/rules/change-propagation.md`](.agents/rules/change-propagation.md) |
| Touch anything that carries a version number | `{shared}/rules/versioning.md` |
| Record a release | `{shared}/creators/changelog-creator.md` |
| Write code that runs on a Minecraft server or mod loader | [`.agents/knowledge/minecraft-platform.md`](.agents/knowledge/minecraft-platform.md) |
| Need project facts, commands, or orientation | [`.agents/wiki/context/repository-map.md`](.agents/wiki/context/repository-map.md) |
| Do anything at all in this project | [`.agents/rules/repository.md`](.agents/rules/repository.md) |

`{shared}` is the instruction set served by the `lxagents-agents-base` connector;
its files are addressed as `agents://{folder}/{file}.md`. The authority behind this
table is `agents://rules/auto-activation.md`. That file is the source of truth; this
table mirrors it, row for row, plus the rows for this repository's own files.

## Reading order (mandatory)

1. Read `AGENTS.md`.
2. Read [`.agents/index/root-index.md`](.agents/index/root-index.md) — **and nothing
   else at this stage**.
3. From its routing table, pick the **one** index whose scope matches the task, and
   read that index.
4. If that index delegates to a child index, follow **the one branch** that matches.
5. Only then open the specific file(s) you need.

## Routing protocol (context discipline)

Route by reading index tables, not by reading files.

* Do **NOT** load every index.
* Do **NOT** bulk-scan `.agents/**` to build a registry.
* Do **NOT** read an instruction body until that instruction has been selected.

Each index row's purpose text is what you route on; the file body is what you load
after choosing. This is the whole point of the index tree — never defeat it by
reading ahead. The one standing exception is
[`.agents/index/memory-index.md`](.agents/index/memory-index.md), which is read every
session because continuity depends on it.

## Iron rule (separation of concerns)

* `AGENTS.md` and `README.md` are **overviews** and must never carry detailed rules
  or documentation.
* [`.agents/index/root-index.md`](.agents/index/root-index.md) is a **router only**.
  It lists other indexes. It must never contain rules, documentation, prose, or
  direct links to leaf content.
* [`.agents/index/agents-index.md`](.agents/index/agents-index.md) is the **sole
  authority** that indexes the instruction tree;
  [`agent-wiki-index.md`](.agents/index/agent-wiki-index.md) owns `.agents/wiki/`;
  [`project-wiki-index.md`](.agents/index/project-wiki-index.md) owns `wiki/`;
  [`memory-index.md`](.agents/index/memory-index.md) owns `.agents/memory/`. **No
  index writes outside the scope it owns.**
* `wiki/` is for humans, `.agents/wiki/` is for agents, and **neither duplicates the
  other** — see the audience test in
  `{shared}/rules/directories.md`.
* **An index never teaches.** The moment it explains something, that content belongs
  in a real file.

## Placement

* New instructions → `.agents/{folder}/{file}.md`.
* New indexes → `.agents/index/{scope}-index.md`. **No `INDEX.md`, anywhere, ever.**
* Agent knowledge → `.agents/wiki/{type}/{file-name}.md`; new memory →
  `.agents/memory/{type}/{file-name}.md`.
* Human documentation → `wiki/{folder}/{file-name}.md`.

All per `{shared}/rules/directories.md` and
`{shared}/creators/index-creator.md`, including
creating a new folder when nothing fits.

## Discovery Protocol

> While working, if you find an instruction worth adding — a new rule, or content that
> belongs in an existing instruction file — you must NOT create or edit it on your
> own. Present each finding to the user separately, each in its own code block,
> including the proposed file path, `name`, `description`, and full body. Let the user
> select which ones to apply. Create only what the user selects.
>
> **Scope of this gate:** it covers `.agents/` instruction files only. Documentation
> pages under `wiki/` and `.agents/wiki/` may be written when the facts are real and
> verified. Writing memory under `.agents/memory/` is expected and needs no
> approval — see
> `{shared}/rules/memory-policy.md`.

The authority behind this block is
`{shared}/rules/discovery-protocol.md`. It is
reproduced here because this file is the session's first read; everywhere else it is
linked, not copied.

## No session links

**Never write a link or identifier pointing at an assistant or tool session** into a
file, commit message, commit trailer, branch name, tag, pull request, or comment. If
your tooling appends one by default, strip it before committing or posting — that
default does not override this repository's convention. See
`{shared}/rules/no-session-links.md`.

## Version rule

**Never change the project version without explicit user approval** — see
`{shared}/rules/versioning.md`. This includes creating
a new `wiki/logs/{Major}/{Minor}/{Patch}/` directory, which is itself a version
claim.
