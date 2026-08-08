# 0.2.0

Released: 2026-08-08

Credentials move into core completely. In `0.1.0` core owned the *rotation*;
consumers still owned the *files*, which meant a key had to be pasted into every
MCAgents plugin separately and rotated in each of them. Now there is one file per
side, owned by core, and a consumer never sees a token at all.

## Added

- **`config.yml` in the core plugin** — the single place API tokens are
  configured on a server. Every MCAgents plugin reads its credentials from here
  and has no token settings of its own.
- **`/mcagents status`** — which platforms have a usable token, which are
  unconfigured, and which have had every token rejected. Never prints a token.
- **`/mcagents reload`** — re-reads `config.yml`, so a key can be replaced
  without restarting the server. Both subcommands sit behind `mcagents.admin`,
  default op.
- **The mod side store** — `MinecraftDirectory` resolves the game directory per
  operating system, honouring `MCAGENTS_DIR` and the loader's own directory
  before falling back to a convention; `SharedTokenStore` reads and writes
  `mcagents.json` atomically, re-reading before each write so two mods cannot
  clobber each other, and never overwriting a malformed file.

## Changed

- The core plugin registers a credential store per vendor at enable, replacing
  the previous behaviour of registering nothing and leaving credentials to the
  consumer.

## Fixed

- `/mcagents status` used `getPluginMeta()`, which is Paper-only and would have
  failed at runtime on SpigotMC. It now uses the Bukkit-level
  `getDescription()`.
