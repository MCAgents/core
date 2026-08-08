package io.github.mcagents.core.common;

import io.github.mcagents.core.api.AgentException;
import io.github.mcagents.core.api.AgentProvider;
import io.github.mcagents.core.api.chat.ChatRequest;
import io.github.mcagents.core.api.chat.ChatResponse;
import io.github.mcagents.core.api.llm.LlmCredentials;
import io.github.mcagents.core.api.llm.LlmVendor;
import io.github.mcagents.core.api.llm.ModelInfo;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * The single entry point to the MCAgents core.
 *
 * <p>This is the only class a consumer needs to read. Everything the core can
 * do is a method here, and everything behind it — the HTTP transport, the four
 * vendor dialects, the client registry — is package-private and unreachable
 * from outside. There is no second way in.</p>
 *
 * <h2>Getting one</h2>
 *
 * <pre>{@code
 * MCAgentsProvider agents = MCAgentsProvider.create();
 * agents.registerOpenRouter(config.getString("openrouter.key"));
 * agents.registerAnthropic(config.getString("anthropic.key"));
 * }</pre>
 *
 * <p>Or, for a single vendor, {@link #openRouter(String)},
 * {@link #openAi(String)}, {@link #deepSeek(String)}, and
 * {@link #anthropic(String)} build a provider with that vendor already
 * registered.</p>
 *
 * <h2>Asking something</h2>
 *
 * <pre>{@code
 * agents.askAnthropic("claude-opus-4", "Name this village in three words.")
 *       .thenAccept(reply -> scheduler.run(() -> villager.setName(reply)))
 *       .exceptionally(failure -> { logger.warning(failure.getMessage()); return null; });
 * }</pre>
 *
 * <h2>Two rules worth knowing before you build on this</h2>
 *
 * <p><strong>Nothing is remembered.</strong> The provider holds no conversation,
 * no cache, and no per player state — the registered vendors are the whole of
 * its memory. A caller that wants a multi turn conversation keeps the history
 * itself and replays it, which is what {@link ChatResponse#asMessage()} is
 * for. This is deliberate: a shared core that quietly accumulated per player
 * history would leak memory in every plugin that used it.</p>
 *
 * <p><strong>Nothing blocks.</strong> Every remote call returns a
 * {@link CompletableFuture} and does its work off the calling thread, so a call
 * is safe from a tick — but the completion is not. Nothing guarantees which
 * thread a future finishes on, so a callback that touches the world, an entity,
 * or an inventory must hop back onto the right scheduler first. On Folia that
 * means the scheduler owning the region, not a global one.</p>
 *
 * <h2>Failure</h2>
 *
 * <p>Failures arrive as a failed future whose cause is always an
 * {@link AgentException} — carrying the vendor, the HTTP status when there was
 * one, and helpers such as {@link AgentException#isRateLimited()}. Only
 * argument validation throws directly, because that is a programming error
 * rather than a remote failure.</p>
 */
public class MCAgentsProvider implements AgentProvider {

    /**
     * The provider installed by the most recent construction — the handle a
     * plugin or mod hands to other code, and the one third party consumers look
     * for.
     *
     * <p>Assigned by the constructor and never cleared, so it survives a
     * {@link #close()}; a closed provider left here fails every call with a
     * clear message rather than turning into a {@code null} dereference in the
     * caller. Declared {@code volatile} because it is installed during plugin
     * enable but read from asynchronous callbacks and, on Folia, from region
     * threads.</p>
     */
    public static volatile MCAgentsProvider instance;

    /**
     * Which vendors are configured, and the client serving each.
     *
     * <p>Deliberately private and package-private in type: no module outside
     * {@code common} can hold a client, which is what keeps this facade the
     * only route to a language model.</p>
     */
    private final ClientRegistry clients;

    /**
     * Creates an empty provider and installs it as {@link #instance}.
     *
     * <p>No vendor is registered and no connection is opened. Register the
     * vendors the server has keys for before calling anything.</p>
     */
    public MCAgentsProvider() {
        this.clients = new ClientRegistry();
        instance = this;
    }

    /**
     * Creates an empty provider.
     *
     * @return The new provider, which is also {@link #instance}.
     */
    public static MCAgentsProvider create() {
        return new MCAgentsProvider();
    }

    /**
     * Creates a provider with vendors already registered.
     *
     * <p>The usual shape when keys come from a config file that has already
     * been read.</p>
     *
     * @param credentials The vendors to register, in order. Registering the
     *                    same vendor twice keeps the last one.
     * @return The new provider, which is also {@link #instance}.
     * @throws NullPointerException When the array or any element is
     *                             {@code null}.
     */
    public static MCAgentsProvider create(LlmCredentials... credentials) {
        Objects.requireNonNull(credentials, "credentials cannot be null");

        MCAgentsProvider provider = new MCAgentsProvider();
        for (LlmCredentials entry : credentials) {
            provider.register(entry);
        }
        return provider;
    }

    /**
     * Creates a provider serving OpenRouter alone.
     *
     * @param apiKey The OpenRouter API key.
     * @return The new provider, which is also {@link #instance}.
     */
    public static MCAgentsProvider openRouter(String apiKey) {
        return create(LlmCredentials.of(LlmVendor.OPENROUTER, apiKey));
    }

    /**
     * Creates a provider serving OpenAI alone.
     *
     * @param apiKey The OpenAI API key.
     * @return The new provider, which is also {@link #instance}.
     */
    public static MCAgentsProvider openAi(String apiKey) {
        return create(LlmCredentials.of(LlmVendor.OPENAI, apiKey));
    }

    /**
     * Creates a provider serving DeepSeek alone.
     *
     * @param apiKey The DeepSeek API key.
     * @return The new provider, which is also {@link #instance}.
     */
    public static MCAgentsProvider deepSeek(String apiKey) {
        return create(LlmCredentials.of(LlmVendor.DEEPSEEK, apiKey));
    }

    /**
     * Creates a provider serving Anthropic alone.
     *
     * @param apiKey The Anthropic API key.
     * @return The new provider, which is also {@link #instance}.
     */
    public static MCAgentsProvider anthropic(String apiKey) {
        return create(LlmCredentials.of(LlmVendor.ANTHROPIC, apiKey));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(LlmCredentials credentials) {
        clients.register(credentials);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregister(LlmVendor vendor) {
        return clients.unregister(vendor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRegistered(LlmVendor vendor) {
        return vendor != null && clients.isRegistered(vendor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<LlmVendor> registeredVendors() {
        return clients.vendors();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ChatResponse> chat(LlmVendor vendor, ChatRequest request) {
        Objects.requireNonNull(vendor, "vendor cannot be null");
        Objects.requireNonNull(request, "request cannot be null");

        try {
            return clients.require(vendor).chat(request);
        } catch (AgentException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<String> ask(LlmVendor vendor, String model, String prompt) {
        Objects.requireNonNull(model, "model cannot be null");
        Objects.requireNonNull(prompt, "prompt cannot be null");

        return chat(vendor, ChatRequest.of(model, prompt)).thenApply(ChatResponse::content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<ModelInfo>> listModels(LlmVendor vendor) {
        Objects.requireNonNull(vendor, "vendor cannot be null");

        try {
            return clients.require(vendor).listModels();
        } catch (AgentException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> ping(LlmVendor vendor) {
        if (vendor == null || !clients.isRegistered(vendor)) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            return clients.require(vendor).ping();
        } catch (AgentException e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Sends a single prompt to OpenRouter and returns just the reply text.
     *
     * <p>OpenRouter namespaces its models by provider, so a model identifier
     * here looks like {@code "anthropic/claude-opus-4"}.</p>
     *
     * @param model The OpenRouter model identifier.
     * @param prompt The single user turn.
     * @return A CompletableFuture containing the reply text.
     */
    public CompletableFuture<String> askOpenRouter(String model, String prompt) {
        return ask(LlmVendor.OPENROUTER, model, prompt);
    }

    /**
     * Sends a single prompt to OpenAI and returns just the reply text.
     *
     * @param model The OpenAI model identifier, for example
     *              {@code "gpt-4o-mini"}.
     * @param prompt The single user turn.
     * @return A CompletableFuture containing the reply text.
     */
    public CompletableFuture<String> askOpenAi(String model, String prompt) {
        return ask(LlmVendor.OPENAI, model, prompt);
    }

    /**
     * Sends a single prompt to DeepSeek and returns just the reply text.
     *
     * @param model The DeepSeek model identifier, for example
     *              {@code "deepseek-chat"}.
     * @param prompt The single user turn.
     * @return A CompletableFuture containing the reply text.
     */
    public CompletableFuture<String> askDeepSeek(String model, String prompt) {
        return ask(LlmVendor.DEEPSEEK, model, prompt);
    }

    /**
     * Sends a single prompt to Anthropic and returns just the reply text.
     *
     * @param model The Anthropic model identifier, for example
     *              {@code "claude-opus-4"}.
     * @param prompt The single user turn.
     * @return A CompletableFuture containing the reply text.
     */
    public CompletableFuture<String> askAnthropic(String model, String prompt) {
        return ask(LlmVendor.ANTHROPIC, model, prompt);
    }

    /**
     * Sends a full exchange to OpenRouter.
     *
     * @param request What to ask, including the model and the full message
     *                list.
     * @return A CompletableFuture containing the reply.
     */
    public CompletableFuture<ChatResponse> chatOpenRouter(ChatRequest request) {
        return chat(LlmVendor.OPENROUTER, request);
    }

    /**
     * Sends a full exchange to OpenAI.
     *
     * @param request What to ask, including the model and the full message
     *                list.
     * @return A CompletableFuture containing the reply.
     */
    public CompletableFuture<ChatResponse> chatOpenAi(ChatRequest request) {
        return chat(LlmVendor.OPENAI, request);
    }

    /**
     * Sends a full exchange to DeepSeek.
     *
     * @param request What to ask, including the model and the full message
     *                list.
     * @return A CompletableFuture containing the reply.
     */
    public CompletableFuture<ChatResponse> chatDeepSeek(ChatRequest request) {
        return chat(LlmVendor.DEEPSEEK, request);
    }

    /**
     * Sends a full exchange to Anthropic.
     *
     * <p>Framing instructions are moved to the top level {@code system} field
     * the Messages API expects, and a token bound is supplied when the request
     * set none, because Anthropic requires one.</p>
     *
     * @param request What to ask, including the model and the full message
     *                list.
     * @return A CompletableFuture containing the reply.
     */
    public CompletableFuture<ChatResponse> chatAnthropic(ChatRequest request) {
        return chat(LlmVendor.ANTHROPIC, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        clients.close();
    }
}
