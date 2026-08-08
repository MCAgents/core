/**
 * The language model transport contracts.
 *
 * <p>{@link io.github.mcagents.core.api.llm.LlmVendor} names the four services
 * the core speaks to, {@link io.github.mcagents.core.api.llm.LlmCredentials}
 * carries what is needed to reach one, and
 * {@link io.github.mcagents.core.api.llm.LlmClient} is the contract an
 * implementation of that conversation satisfies.</p>
 *
 * <p>Nothing here performs any I/O or names an HTTP type. The contracts say
 * what a client does, not how it does it; the implementations live in
 * {@code io.github.mcagents.core.common}.</p>
 */
package io.github.mcagents.core.api.llm;
