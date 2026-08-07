# core

Shared foundation repository for **MCAgents**. It is intended to hold the agent
and API code that MCAgents' Minecraft plugins and mods build on, so that behavior
common to several projects lives in one place instead of being copied between
them. The repository is at the setup stage — it currently contains the agent
instruction set and the documentation structure, and no source code yet.

## What is here today

- An agent instruction set under `.agents/` — placement rules, git conventions,
  task workflow, and the creator agents that maintain the trees.
- A documentation tree under `wiki/`.
- An `INDEX.md` router tree so a file can be found by reading a few small tables
  instead of scanning the repository.

## Quick start

```sh
git clone https://github.com/MCAgents/core.git
cd core
```

Only git is required — there is nothing to build or install yet. Full details:
[`wiki/environments/setup.md`](wiki/environments/setup.md).

## Documentation

Start at [`wiki/INDEX.md`](wiki/INDEX.md).

- [`wiki/information/overview.md`](wiki/information/overview.md) — what this
  repository is and how it is organized.
- [`wiki/environments/setup.md`](wiki/environments/setup.md) — getting a working
  copy and starting a change.
- [`AGENTS.md`](AGENTS.md) — entry point for agents working in this repository.

## License

MIT — see [`LICENSE`](LICENSE).
