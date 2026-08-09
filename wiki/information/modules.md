# Modules

The build is a Gradle multi project defined in `settings.gradle`, with
`rootProject.name = mcagents`. It has twelve modules, and every package under
them is prefixed `io.github.mcagents.core`.

## The module graph

Each platform family lives in one folder, and the folder's `core` module holds
what the rest of that family shares. Nothing sits loose at the `platforms/`
level except the engine, which is not a family — it is the thing that bundles
them all.

```
api          pure Java contracts
 └── common  pure Java implementations

platforms/bukkit/core        shared Bukkit code (Spigot API)
 ├── platforms/bukkit/spigotmc   SpigotMC entry point
 ├── platforms/bukkit/papermc    PaperMC entry point
 └── platforms/bukkit/foliamc    Folia entry point

platforms/mods/core          shared mod code, both physical sides
 ├── platforms/mods/client       client-only half
 ├── platforms/mods/server       dedicated-server-only half
 ├── platforms/mods/neoforge     NeoForge entry point
 └── platforms/mods/fabric       Fabric entry point

platforms/engine             implements every module above
```

## What each module holds

| Module | Package | Purpose |
|---|---|---|
| `api` | `…core.api` | Pure Java contracts: interfaces, records, enums, abstract types, and the `token` package holding the credential storage contract. No implementation, and no platform type anywhere. |
| `common` | `…core.common` | Pure Java implementations of those contracts. Still no platform type. |
| `platforms:bukkit:core` | `…core.bukkit` | The code SpigotMC, PaperMC, and Folia share, compiled against the Spigot API. |
| `platforms:bukkit:spigotmc` | `…core.spigotmc` | Only what SpigotMC needs on top of `platforms:bukkit:core`. |
| `platforms:bukkit:papermc` | `…core.papermc` | Only what PaperMC needs on top of `platforms:bukkit:core`. |
| `platforms:bukkit:foliamc` | `…core.foliamc` | Only what Folia needs on top of `platforms:bukkit:core`, which in practice means the regionised schedulers. |
| `platforms:mods:core` | `…core.mods` | The code every mod module shares — the same role `platforms:bukkit:core` plays for the server side — including the machinery that decides which physical side is running. |
| `platforms:mods:client` | `…core.mods.client` | The client-only half: the player's own credential file, the client commands, anything drawn on screen. |
| `platforms:mods:server` | `…core.mods.server` | The dedicated-server-only half: the server's credential file, and the checks that decide who may do what. |
| `platforms:mods:neoforge` | `…core.neoforge` | Only what NeoForge needs on top of `platforms:mods:core`. |
| `platforms:mods:fabric` | `…core.fabric` | Only what Fabric needs on top of `platforms:mods:core`. |
| `platforms:engine` | `…core.engine` | The universal entry point. The one module that implements every other module, so a single artifact carries the whole core. |

## Why the platform modules carry a qualified group

Both families hold a module named `core`, and Gradle identifies a project by
`group:name` — never by its path. On one shared group they would both be
`io.github.mcagents:core`, which is to say **the same module**, and Gradle would
resolve one away in favour of the other.

Nothing would fail. `./gradlew build` would stay green and the engine would
shade eleven modules and ship without the twelfth.

So the root `build.gradle` qualifies a platform module's group with its family
folder: `io.github.mcagents.bukkit:core` and `io.github.mcagents.mods:core` are
distinct modules, and the folders keep the names that read best. Only the
platform modules are affected and none of them is published, so no coordinate
anyone depends on changes.

If a third family is ever added, this is why its shared module may also be
called `core`.

## Why the mod family has two sides and the Bukkit family does not

A Bukkit plugin only ever runs on a server. A mod runs in two physically
different places — a client with a window and a player in front of it, and a
dedicated server with neither — and the classes that exist differ between them.
Client-only code reached on a dedicated server is not a logic error; it is a
`NoClassDefFoundError` that takes the server down.

So the mod family splits by **physical side** rather than by loader:
`platforms:mods:client` and `platforms:mods:server` never depend on each other,
and neither is named by type from anywhere else. The loaders — NeoForge and
Fabric — sit beside them and differ only in how they announce an entry point,
which is a much smaller difference than the one between the two sides.

How the right half is started without linking the other is on
[`mod-sides.md`](mod-sides.md).

## The distributed artifact is a Bukkit plugin

`MCAgents-{version}.jar` is not a library you drop on a classpath — it is a
**plugin the server loads**, named `MCAgents` in its `plugin.yml`. That name is
the contract: a consumer plugin declares `depend: [MCAgents]`, which is what
makes Bukkit load this one first and expose its classes to the consumer's
bridge.

It is a service plugin. It registers no commands, no permissions, and no
listeners, and a player never sees it do anything. `onEnable` builds a
`MCAgentsProvider` and installs it as `MCAgentsProvider.instance`; `onDisable`
closes it. Credentials are not its business — they belong to whichever consumer
plugin has a configuration file.

`folia-supported: true` is declared because the provider schedules nothing and
touches no game state: every call is a `CompletableFuture` over an HTTP client,
so regionised multithreading has nothing here to break.

## Why the dependencies are `compileOnly`

Every module below the engine declares `api`, `common`, and its family core
(`platforms:bukkit:core` or `platforms:mods:core`) as **`compileOnly`**. They
compile against those classes but never bundle them.

`platforms:engine` is the one exception: it declares every module as
`implementation` and shades them into `MCAgents-{version}.jar`. That is what
keeps exactly one copy of each class in the distributed artifact instead of one
copy per module that happened to depend on it.

## Published artifacts

Only `api` and `common` carry a publication:

| Module | Coordinates |
|---|---|
| `api` | `io.github.mcagents:mcagents-api` |
| `common` | `io.github.mcagents:mcagents-common` |

The `artifactId` is qualified with the root project name deliberately. Without
it, every MCAgents project in this group would publish a colliding
`io.github.mcagents:api`, and a consumer could not depend on `core` and another
project at the same time.

The platform modules are distributed as plugin and mod artifacts rather than
libraries, so they are not published to a Maven repository.

## Platform API versions

The platform coordinates live in `gradle/libs.versions.toml`, so a version bump
is one change in one file:

| Catalog entry | Coordinate | Used by |
|---|---|---|
| `gson` | `com.google.code.gson:gson` | `common`, the mod family |
| `spigot-api` | `org.spigotmc:spigot-api` | `platforms:bukkit:core`, `platforms:bukkit:spigotmc` |
| `paper-api` | `io.papermc.paper:paper-api` | `platforms:bukkit:papermc` |
| `folia-api` | `dev.folia:folia-api` | `platforms:bukkit:foliamc`, `platforms:engine` |

Gson is `compileOnly` like the platform APIs, and for the same reason: every
target already provides it. The Bukkit family bundles it in the server jar, and
Minecraft itself pulls it in for the mod loaders, so the shaded engine jar
deliberately carries no copy of it.

The engine compiles against the Folia API alone. Folia is a superset of Paper,
which is a superset of Spigot, and all three declare the same
`org.spigotmc:spigot-api` capability — Gradle rejects them as a mutually
exclusive conflict if they are declared together, so the superset is the only
one that can be named.

No module under `platforms/mods` declares a **mod loader coordinate yet**.
Resolving `net.neoforged:neoforge` or the Fabric loader requires a toolchain
(ModDevGradle, Loom) that remaps Minecraft as part of the build. Those are
introduced together with the first real loader code rather than scaffolded
ahead of it. Everything the mod family holds today is plain Java, which is also
what keeps it testable without a game running.

## Java version

Every module compiles on **Java 25**, set once in the root `build.gradle` through
a Gradle toolchain. See [`../environments/setup.md`](../environments/setup.md).
