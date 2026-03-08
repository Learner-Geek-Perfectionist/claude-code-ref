# Smart Shortest Unique Suffix Path

## Problem

The extension copies file references as `@path#line` to clipboard for Claude Code CLI.
- Workspace-relative paths can be long for deeply nested files
- Basename-only references are ambiguous when multiple files share the same name
- Non-workspace files have very long absolute paths

## Solution

Compute the **shortest unique suffix path** for workspace files — the minimum path segments needed to uniquely identify a file among all same-named files in the workspace.

## Algorithm

1. Get the basename of the current file
2. Use `vscode.workspace.findFiles('**/' + basename)` to find all files with the same name
3. If only 1 match → use basename alone
4. If multiple matches → incrementally prepend parent directory segments until the suffix is unique

Example:
```
Workspace files:
  src/components/Button/index.ts
  src/pages/Home/index.ts
  src/utils/format.ts

Generated references:
  @Button/index.ts#5       ← shortest unique suffix (2 matches for index.ts)
  @Home/index.ts#10        ← shortest unique suffix
  @format.ts#3             ← unique basename, no suffix needed
```

## Scope

| Scenario | Strategy |
|----------|----------|
| Workspace file, unique name | basename only |
| Workspace file, duplicate name | shortest unique suffix |
| Non-workspace file | keep current behavior (full path) |

## Performance

`vscode.workspace.findFiles` uses VS Code's file index, excludes `node_modules`/`.git` by default, and returns in milliseconds.
