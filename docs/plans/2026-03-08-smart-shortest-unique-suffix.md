# Smart Shortest Unique Suffix Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace full relative paths with the shortest unique suffix path in clipboard references.

**Architecture:** Extract a `getShortestUniquePath()` function that uses `vscode.workspace.findFiles` to find same-named files, then computes the minimum path suffix needed for uniqueness. Integrate it into the existing command handler.

**Tech Stack:** TypeScript, VS Code Extension API (`workspace.findFiles`)

---

### Task 1: Extract `getShortestUniquePath` function

**Files:**
- Modify: `src/extension.ts` (add function before `activate`)

**Step 1: Add the helper function**

Add before the `activate` function:

```typescript
async function getShortestUniquePath(
  doc: vscode.TextDocument
): Promise<string> {
  const basename = path.basename(doc.fileName);
  const relativePath = vscode.workspace.asRelativePath(doc.uri);

  // Find all workspace files with the same basename
  const matches = await vscode.workspace.findFiles(`**/${basename}`);

  // Unique name — just use basename
  if (matches.length <= 1) {
    return basename;
  }

  // Multiple matches — find shortest unique suffix
  const allRelativePaths = matches.map(uri =>
    vscode.workspace.asRelativePath(uri)
  );
  const parts = relativePath.split('/');

  for (let suffixLen = 1; suffixLen <= parts.length; suffixLen++) {
    const suffix = parts.slice(-suffixLen).join('/');
    const count = allRelativePaths.filter(p => p.endsWith(suffix)).length;
    if (count === 1) {
      return suffix;
    }
  }

  // Fallback: full relative path
  return relativePath;
}
```

**Step 2: Commit**

```bash
git add src/extension.ts
git commit -m "feat: add getShortestUniquePath helper function"
```

---

### Task 2: Integrate into command handler

**Files:**
- Modify: `src/extension.ts:14-18` (replace `relativePath` computation)

**Step 1: Replace path computation in handler**

Replace lines 14-18:

```typescript
      const doc = editor.document;
      const workspaceFolder = vscode.workspace.getWorkspaceFolder(doc.uri);
      const relativePath = workspaceFolder
        ? vscode.workspace.asRelativePath(doc.uri)
        : doc.fileName;
```

With:

```typescript
      const doc = editor.document;
      const workspaceFolder = vscode.workspace.getWorkspaceFolder(doc.uri);
      const relativePath = workspaceFolder
        ? await getShortestUniquePath(doc)
        : doc.fileName;
```

**Step 2: Compile and verify no errors**

Run: `npm run compile`
Expected: Clean compilation, no errors.

**Step 3: Commit**

```bash
git add src/extension.ts
git commit -m "feat: use shortest unique suffix for workspace file references"
```

---

### Task 3: Manual verification

**Step 1: Test with unique filename**

1. Open a workspace with a uniquely-named file (e.g. `extension.ts`)
2. Place cursor on a line, press `Alt+K`
3. Paste clipboard content
4. Expected: `@extension.ts#<line>` (basename only)

**Step 2: Test with duplicate filenames**

1. Open a workspace with multiple `index.ts` files in different directories
2. Open one of them, press `Alt+K`
3. Paste clipboard content
4. Expected: `@<parent>/index.ts#<line>` (shortest unique suffix)

**Step 3: Test with non-workspace file**

1. Open a file outside the workspace
2. Press `Alt+K`
3. Expected: full path (unchanged behavior)

**Step 4: Package extension**

Run: `npm run compile && npx vsce package`
Expected: `.vsix` file generated successfully.

**Step 5: Commit packaged extension**

```bash
git add claude-code-ref-*.vsix
git commit -m "chore: package extension v0.1.1"
```
