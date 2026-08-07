---
name: minecraft-platform-knowledge
description: Platform targets — Spigot, Paper, and Folia — their API hierarchy, which way compatibility runs, and Folia's threading constraints.
---

# Minecraft Platform

## Supported platforms

`core` targets these three server platforms, and only these three for now:

| Platform | What it is | API roots |
|---|---|---|
| **Spigot** | A CraftBukkit fork implementing the Bukkit API. | `org.bukkit.*`, `org.spigotmc.*` |
| **Paper** | A fork of Spigot. Superset of the Spigot API, plus its own. | Spigot roots, plus `io.papermc.paper.*` |
| **Folia** | A PaperMC fork of Paper with regionised multithreading. | Paper roots, plus the regionised schedulers |

Other loaders and platforms — Fabric, Forge, NeoForge, Sponge, and the proxies
(Velocity, BungeeCord) — are **out of scope**. Do not write code, documentation,
or instructions targeting them until that decision changes.

## The hierarchy — compatibility runs one way

```
Bukkit API
 └── Spigot        implements Bukkit, adds the Spigot API
      └── Paper    implements Spigot, adds the Paper API
           └── Folia   implements Paper, changes the threading model
```

* Code written against the **Bukkit/Spigot API runs on all three**.
* Code touching **Paper-only API does not run on Spigot**. This fails at runtime —
  a missing class or method when the code path is first hit — **not** at compile
  time. A green build proves nothing about Spigot compatibility.
* **Folia runs Paper plugins only if they respect its threading model** and opt in
  explicitly.

**Rule:** target the lowest platform that provides what you need. Reach for
Paper-only API when Spigot genuinely cannot do the job, and record why in the code.

## Folia — the constraint that actually matters

Folia replaces "one main server thread" with **regionised multithreading**: the
world is split into independent regions, each ticked by its own thread. Habits
carried over from Spigot and Paper break here, usually silently.

What breaks:

* **`Bukkit.getScheduler()`** — the `BukkitScheduler` is unsupported on Folia.
  Treat any use of it as Folia-incompatible.
* **"I am on the main thread"** — there is no single main thread. Do not use
  `Bukkit.isPrimaryThread()` as proof that access is safe; it does not mean on
  Folia what it means on Paper.
* **Static mutable state and shared collections** — several region threads may
  reach them at once. Unsynchronized shared state is a data race, not a style
  preference.
* **Cross-region access** — touching an entity, chunk, or block owned by another
  region from the wrong thread is unsafe. Schedule the work onto the owning region
  instead of reaching across.

Use the regionised schedulers:

| Scheduler | Obtain with | Use for |
|---|---|---|
| Global region | `Bukkit.getGlobalRegionScheduler()` | Server-wide state — time, weather, global config. |
| Region | `Bukkit.getRegionScheduler()` | Work tied to a specific location or chunk. |
| Entity | `entity.getScheduler()` | Work that follows an entity as it moves between regions. |
| Async | `Bukkit.getAsyncScheduler()` | Off-thread work that touches no game state. |

**Opting in:** a plugin loads on Folia only when it declares `folia-supported: true`
in its plugin manifest. That flag is a **claim that the code honours the rules
above** — not a switch that makes it true. Do not set it before the code earns it.

## What this means for `core`

`core` is a shared foundation consumed by several plugins and mods, so a mistake
here propagates to every consumer:

* **Write to the lowest common denominator by default** — the Bukkit/Spigot API —
  so a consumer on any of the three platforms can use it.
* **Never assume a main thread in `core`.** Shared code that calls
  `Bukkit.getScheduler()` makes every downstream consumer Folia-incompatible, even
  the ones that did everything right.
* **Isolate and clearly mark platform-specific code**, so a consumer can tell what
  runs where without reading the implementation.
* **Thread-safety is part of `core`'s public contract.** For anything `core`
  exposes, document which thread or region it may be called from.

## Not yet decided

Do not fill these in by guessing:

* The **Minecraft versions** `core` targets. Until they are decided, write no
  version-specific code, no compatibility shims, and no documentation claiming a
  supported range.
* The **build system and API dependency coordinates** — none exist in this
  repository yet. See [`../rules/repository.md`](../rules/repository.md).

Record each one here in the same change that introduces it.

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
