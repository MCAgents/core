---
name: logs-index
description: Index of versioned change logs under wiki/logs/ — one row per version directory, newest first.
---

# Logs Index

**Scope:** `wiki/logs/`
**Parent:** [wiki-index](../INDEX.md)

Every version directory is `wiki/logs/{Major}/{Minor}/{Patch}/`, numeric
segments only — no `v` prefix, no zero padding.

**Creating a new version directory is a version claim** and requires user
approval — see [`../../.agents/rules/versioning.md`](../../.agents/rules/versioning.md).

## Versions

| Version | Summary | Files |
|---|---|---|
| [`0/2/0/`](0/2/0/CHANGELOG.md) | Credentials move into core entirely: its own config.yml, the shared mcagents.json, and the /mcagents command. | `CHANGELOG.md` |
| [`0/1/0/`](0/1/0/CHANGELOG.md) | First functional release: the agent API, four vendors, a loadable Bukkit plugin, and credential pooling. | `CHANGELOG.md` |
