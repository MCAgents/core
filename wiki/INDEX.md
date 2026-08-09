---
name: wiki-index
description: Index of the wiki/ documentation tree — project information, environment setup, and the versioned change logs.
---

# Wiki Index

**Scope:** `wiki/`
**Parent:** [root-index](../INDEX.md)

This index owns `wiki/` and only `wiki/`. It must never write into `.agents/`.
Any page added to or removed from `wiki/` is reflected here **in the same commit**.

## Information

| File | Purpose |
|---|---|
| [`information/overview.md`](information/overview.md) | What this repository is, what it is for, and what it contains today. |
| [`information/modules.md`](information/modules.md) | The twelve build modules, how they depend on each other, and what is published. |
| [`information/mod-sides.md`](information/mod-sides.md) | How the mod half runs on a client and a dedicated server from one jar, without either side loading the other's code. |

## Guides

| File | Purpose |
|---|---|
| [`guides/llm-providers.md`](guides/llm-providers.md) | Getting from an API key to a model's reply, and the threading and memory rules that come with it. |

## Reference

| File | Purpose |
|---|---|
| [`reference/api.md`](reference/api.md) | Every public type in the api and common modules, method by method. |

## Environments

| File | Purpose |
|---|---|
| [`environments/setup.md`](environments/setup.md) | Getting a local working copy, and building, testing, and publishing it. |

## Child Indexes

| Index | Scope | Load when |
|---|---|---|
| [`logs/INDEX.md`](logs/INDEX.md) | Versioned change logs under `wiki/logs/` | You need release history, or must record a change against a version. |
