# Local Setup

## Requirements

* **git**
* **A JDK** to run Gradle with. The build itself compiles on **Java 25**, but you
  do not have to install it: the Gradle toolchain support downloads a Java 25 JDK
  the first time you build, using the
  [foojay resolver](https://github.com/gradle/foojay-toolchains) configured in
  `settings.gradle`.
* **Network access** on the first build, to fetch Gradle 9.5.0, the Java 25
  toolchain, and the platform APIs from Maven Central, the SpigotMC repository,
  and the PaperMC repository.

Gradle itself does not need to be installed — use the wrapper (`./gradlew`)
checked into the repository.

## Get a working copy

```sh
git clone https://github.com/MCAgents/core.git
cd core
```

The default branch is `master`.

## Build

```sh
./gradlew build
```

That compiles every module, runs the tests, and produces one jar per module under
`{module}/build/libs/`, including the universal `MCAgents-{version}.jar` shaded by
`platforms:engine`.

Useful variants:

```sh
./gradlew :api:build                 # one module only
./gradlew :platforms:engine:shadowJar  # the universal jar on its own
./gradlew clean                      # delete the root build directory
./gradlew javaToolchains             # show which JDK the build resolved
```

## Install it on a server

Drop `platforms/engine/build/libs/MCAgents-{version}.jar` into the server's
`plugins/` folder. It loads as a plugin named `MCAgents`, which is the name
consumer plugins declare in their `depend` list.

Configure API tokens either by editing `plugins/MCAgents/config.yml`, or in
game:

```
/agents                                   credential status for every platform
/agents reload                            re-read the credential file
/agents <platform> token add <token>      store a token
/agents <platform> token remove <handle>  delete one, handle from tab completion
```

Tab completion offers a **masked handle** such as `#2:a3f9` for `remove` —
position plus the last four characters — so choosing which key to delete never
puts the key itself on screen. The full value is accepted too.

Everything sits behind `mcagents.admin`, default op.

## Test

```sh
./gradlew test
```

## Publish the library modules

`api` and `common` are the two published modules. They go to GitHub Packages as
`io.github.mcagents:mcagents-api` and `io.github.mcagents:mcagents-common`:

```sh
./gradlew publish
```

Publishing reads the `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables for
credentials. To try a release locally without publishing anywhere public:

```sh
./gradlew publishToMavenLocal
```

## Start a piece of work

Never commit to `master`. Branch from it, named `{type}/{primary-noun}`:

```sh
git checkout master
git pull origin master
git checkout -b docs/my-change
```

When the work is done:

```sh
git push -u origin docs/my-change
```

Then open one pull request for the branch. The branch types, commit message
format, pull request title and body rules, and the merge procedure are defined in
`.agents/` — start from [`../../AGENTS.md`](../../AGENTS.md).

## Environment variables

| Variable | Read by | Purpose |
|---|---|---|
| `GITHUB_ACTOR` | `build.gradle` | Username for the GitHub Packages repository. |
| `GITHUB_TOKEN` | `build.gradle` | Token for the GitHub Packages repository. |

Both are only needed when running `./gradlew publish`. Nothing else in the
repository reads the environment.
