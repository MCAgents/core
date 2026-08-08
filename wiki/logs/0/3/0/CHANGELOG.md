# 0.3.0

Released: 2026-08-08

Tokens become manageable from inside the game, and the command gets a shorter
name.

## Upgrading

**`/mcagents` is now `/agents`.** Anything scripted against the old name must be
updated. The permission node is unchanged: `mcagents.admin`.

## Added

- **`/agents <platform> token add <token>`** and
  **`/agents <platform> token remove <handle>`** — store or revoke a key without
  opening a file. The change takes effect on the next request.
- **Masked token handles.** Tab completion for `remove` offers `#2:a3f9` —
  position plus the last four characters — rather than the key itself, so
  choosing which credential to delete never puts a live key into a client's
  suggestion list. The full value is accepted as an argument too.
- **`TokenStore.add`**, implemented for both `config.yml` and `mcagents.json`.
  Both re-read before writing and refuse a duplicate.
- **`ModTokenCommands`** — the client side of the same subcommands and
  suggestions, so both mod loaders share one implementation. The loader entry
  point supplies only the Brigadier wiring.

## Changed

- `/mcagents` renamed to `/agents` in every manifest, message, and document.
- `/agents` with no arguments now shows credential status, including the masked
  handles of every stored key.

## Security notes

- A handle reveals four characters, the same disclosure a payment card gets, and
  is checked against the credential actually in that slot before a removal — so a
  list that shifted between tab completion and execution cannot delete the wrong
  key.
- `token add` completes nothing, because its argument is a secret.
- Adding a token as a player prints a reminder that the value is in the client's
  command history, and suggests using the console.
