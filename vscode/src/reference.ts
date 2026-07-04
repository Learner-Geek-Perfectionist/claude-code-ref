export interface ReferenceSelection {
  isEmpty: boolean;
  activeLine: number;
  startLine: number;
  endLine: number;
  endCharacter: number;
}

export interface ReferenceEditor {
  documentPath: string;
  selections: ReferenceSelection[];
}

type MaybePromise<T> = T | PromiseLike<T>;

export interface ClipboardReferenceCopierDependencies {
  getEditor: () => ReferenceEditor | undefined;
  writeClipboard: (text: string) => MaybePromise<void>;
  onNoEditor: () => MaybePromise<void>;
  onClipboardFailure: (error: unknown) => MaybePromise<void>;
  onSuccess?: () => MaybePromise<void>;
}

export function buildReference(editor: ReferenceEditor): string {
  const refs: string[] = [];

  for (const sel of editor.selections) {
    if (sel.isEmpty) {
      refs.push(`@${editor.documentPath}#${sel.activeLine + 1}`);
    } else {
      let endLine = sel.endLine + 1;
      if (sel.endCharacter === 0 && sel.endLine > sel.startLine) {
        endLine = sel.endLine;
      }
      const startLine = sel.startLine + 1;
      refs.push(
        startLine === endLine
          ? `@${editor.documentPath}#${startLine}`
          : `@${editor.documentPath}#${startLine}-${endLine}`
      );
    }
  }

  return refs.join(' ') + ' ';
}

export function createClipboardReferenceCopier(
  deps: ClipboardReferenceCopierDependencies,
): () => Promise<boolean> {
  return async () => {
    const editor = deps.getEditor();
    if (!editor) {
      await deps.onNoEditor();
      return false;
    }

    const refText = buildReference(editor).trimEnd();

    try {
      await deps.writeClipboard(refText);
    } catch (error: unknown) {
      await deps.onClipboardFailure(error);
      return false;
    }

    await deps.onSuccess?.();
    return true;
  };
}
