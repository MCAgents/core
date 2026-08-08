# 0.1.0

Released: 2026-08-08

The first functional release. `0.0.0` was a scaffold: the modules existed and
compiled, but the jar was not something a server could load and the API had no
credential handling. Both are now true.

## Added

- **The agent API.** `MCAgentsProvider` in `io.github.mcagents.core.common` is
  the single entry point: registration, `chat`, `ask`, `listModels`, `ping`, and
  a named function per vendor. Everything behind it is package-private, so it
  cannot be routed around.
- **Four vendors.** OpenRouter, OpenAI, and DeepSeek through one OpenAI-dialect
  client; Anthropic through its own, which lifts framing instructions to the
  top-level `system` field, supplies the mandatory token bound, and joins the
  reply's text blocks.
- **A Bukkit plugin entry point.** `MCAgents-{version}.jar` now carries a
  `plugin.yml` naming it `MCAgents`, plus entry points for SpigotMC, PaperMC,
  Folia, and a universal jar that detects the fork at enable time. It is a
  service plugin: no commands, no permissions, no configuration.
- **Credential pooling.** `registerStore` hands core a `TokenStore` and core owns
  the lifecycle from there: retry on the next credential when the vendor rejects
  one and delete it from the store, retry and keep it when the vendor rate limits
  one, and touch nothing on any other failure. `TokenState` keeps "never
  configured" distinct from "every one was rejected".
- **Documentation** under `wiki/`: the module graph, the API reference, the
  provider usage guide, and local setup.
- **The agent instruction set** under `.agents/`, including the change
  propagation rule that requires a code change to update the documents it
  invalidates in the same commit.

## Fixed

- **The distributed jar never loaded.** It carried no `plugin.yml` and no
  `JavaPlugin` class, so the server ignored it — and any consumer declaring
  `depend: [MCAgents]` failed to enable as a result.
- **Rotation discarded a custom endpoint.** Rebuilding a credential on rotation
  used the vendor's default base URL, which would have silently moved a proxied
  or self-hosted deployment onto the public endpoint — and then evicted the
  perfectly good key that endpoint rejected. Connection settings are now stored
  per vendor and preserved across every rotation.

## Known gaps

- `TokenUsage` has no field for the cache-hit counts vendors report, so a
  consumer cannot see whether prompt caching is working. Caching still happens.
- Anthropic's explicit `cache_control` markers cannot be expressed through
  `ChatRequest`, so Anthropic does not cache. The other three vendors cache
  automatically.
- The NeoForge and Fabric modules compile but carry no loader code.
