# Modules

The build is a Gradle multi project defined in `settings.gradle`, with
`rootProject.name = mcagents`. It has ten modules, and every package under them
is prefixed `io.github.mcagents.core`.

## The module graph

```
api          pure Java contracts
 └── common  pure Java implementations

platforms/bukkit           shared Bukkit code (Spigot API)
 ├── platforms/spigotmc    SpigotMC entry point
 ├── platforms/papermc     PaperMC entry point
 └── platforms/foliamc     Folia entry point

platforms/mods             shared mod loader code
 ├── platforms/neoforge    NeoForge entry point
 └── platforms/fabric      Fabric entry point

platforms/engine           implements every module above
```

## What each module holds

| Module | Package | Purpose |
|---|---|---|
| `api` | `…core.api` | Pure Java contracts: interfaces, records, enums, abstract types. No implementation, and no platform type anywhere. |
| `common` | `…core.common` | Pure Java implementations of those contracts. Still no platform type. |
| `platforms:bukkit` | `…core.bukkit` | The code SpigotMC, PaperMC, and Folia share, compiled against the Spigot API. |
| `platforms:spigotmc` | `…core.spigotmc` | Only what SpigotMC needs on top of `platforms:bukkit`. |
| `platforms:papermc` | `…core.papermc` | Only what PaperMC needs on top of `platforms:bukkit`. |
| `platforms:foliamc` | `…core.foliamc` | Only what Folia needs on top of `platforms:bukkit`, which in practice means the regionised schedulers. |
| `platforms:mods` | `…core.mods` | The code the mod loaders share — the same role `platforms:bukkit` plays for the server side. |
| `platforms:neoforge` | `…core.neoforge` | Only what NeoForge needs on top of `platforms:mods`. |
| `platforms:fabric` | `…core.fabric` | Only what Fabric needs on top of `platforms:mods`. |
| `platforms:engine` | `…core.engine` | The universal entry point. The one module that implements every other module, so a single artifact carries the whole core. |

## Why the dependencies are `compileOnly`

Every module below the engine declares `api`, `common`, and its family core
(`platforms:bukkit` or `platforms:mods`) as **`compileOnly`**. They compile
against those classes but never bundle them.

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
| `spigot-api` | `org.spigotmc:spigot-api` | `platforms:bukkit`, `platforms:spigotmc` |
| `paper-api` | `io.papermc.paper:paper-api` | `platforms:papermc` |
| `folia-api` | `dev.folia:folia-api` | `platforms:foliamc`, `platforms:engine` |

The engine compiles against the Folia API alone. Folia is a superset of Paper,
which is a superset of Spigot, and all three declare the same
`org.spigotmc:spigot-api` capability — Gradle rejects them as a mutually
exclusive conflict if they are declared together, so the superset is the only
one that can be named.

`platforms:mods`, `platforms:neoforge`, and `platforms:fabric` declare **no mod
loader coordinate yet**. Resolving `net.neoforged:neoforge` or the Fabric loader
requires a toolchain (ModDevGradle, Loom) that remaps Minecraft as part of the
build. Those are introduced together with the first real loader code rather than
scaffolded ahead of it.

## Java version

Every module compiles on **Java 25**, set once in the root `build.gradle` through
a Gradle toolchain. See [`../environments/setup.md`](../environments/setup.md).
