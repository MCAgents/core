---
name: minecraft-platform-knowledge
description: Platform targets — Spigot, Paper, and Folia — their API hierarchy, which way compatibility runs, and Folia's threading constraints.
---

# Minecraft Platform

## Supported platforms

`core` targets two families. On the server side, three Bukkit platforms:

| Platform | What it is | API roots | Module |
|---|---|---|---|
| **Spigot** | A CraftBukkit fork implementing the Bukkit API. | `org.bukkit.*`, `org.spigotmc.*` | `platforms:spigotmc` |
| **Paper** | A fork of Spigot. Superset of the Spigot API, plus its own. | Spigot roots, plus `io.papermc.paper.*` | `platforms:papermc` |
| **Folia** | A PaperMC fork of Paper with regionised multithreading. | Paper roots, plus the regionised schedulers | `platforms:foliamc` |

On the mod side, two loaders:

| Loader | What it is | Module |
|---|---|---|
| **NeoForge** | A Forge fork, and the mainline Forge-lineage loader. | `platforms:neoforge` |
| **Fabric** | A lightweight loader with its own API. | `platforms:fabric` |

`platforms:bukkit` holds what the three server platforms share;
`platforms:mods` plays the same role for the two loaders. `platforms:engine`
implements every module.

**Sponge, legacy Forge, and the proxies (Velocity, BungeeCord) remain out of
scope.** Do not write code, documentation, or instructions targeting them until
that decision changes.

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

* **Write to the lowest common denominator by default** — the Bukkit/Spigot API
  on the server side — so a consumer on any of the three server platforms can use
  it. Better still, write it in `api` or `common`, where it names no platform at
  all and both families get it.
* **Never assume a main thread in `core`.** Shared code that calls
  `Bukkit.getScheduler()` makes every downstream consumer Folia-incompatible, even
  the ones that did everything right.
* **Isolate and clearly mark platform-specific code**, so a consumer can tell what
  runs where without reading the implementation.
* **Thread-safety is part of `core`'s public contract.** For anything `core`
  exposes, document which thread or region it may be called from.

## Dependency coordinates

Every platform coordinate is declared once, in `gradle/libs.versions.toml`, and
never inline in a module:

| Catalog entry | Coordinate | Compiled against by |
|---|---|---|
| `spigot-api` | `org.spigotmc:spigot-api` | `platforms:bukkit`, `platforms:spigotmc` |
| `paper-api` | `io.papermc.paper:paper-api` | `platforms:papermc` |
| `folia-api` | `dev.folia:folia-api` | `platforms:foliamc`, `platforms:engine` |

The engine compiles against the Folia API alone. All three declare the same
`org.spigotmc:spigot-api` capability, so Gradle rejects them as a mutually
exclusive conflict when declared together — only the superset can be named.

**The mod loaders have no coordinate yet.** Resolving `net.neoforged:neoforge`
or the Fabric loader requires a toolchain (ModDevGradle, Loom) that remaps
Minecraft as part of the build. `platforms:mods`, `platforms:neoforge`, and
`platforms:fabric` are plain Java modules until the first real loader code lands
and brings its toolchain with it. Do not add one speculatively.

## Not yet decided

Do not fill these in by guessing:

* The **Minecraft versions** `core` targets. Until they are decided, write no
  version-specific code, no compatibility shims, and no documentation claiming a
  supported range.

Record each one here in the same change that introduces it.

## Changing this rule

This file is an instruction, so it is **not yours to edit on your own initiative** — even
when you are confident it is wrong. Collect the finding and propose it, per
[`../rules/discovery-protocol.md`](../rules/discovery-protocol.md).
