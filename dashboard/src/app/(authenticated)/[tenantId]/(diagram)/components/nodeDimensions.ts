/**
 * Deterministic node footprints for diagram layout.
 *
 * ELK is fed these instead of live-measured DOM sizes, so the layout is a
 * pure function of the graph: re-running it — after a remount, an HMR swap in
 * dev, a zoom change, or with a node selected — reproduces identical
 * geometry. (Measured sizes vary with all of those, which made re-layouts
 * visibly "reshuffle" an already-displayed diagram.)
 *
 * Small glyph nodes are exact (from each component's Tailwind classes:
 * h-6/w-6 = 24, h-8/w-8 = 32, h-10/w-10 = 40). Boxed nodes estimate width
 * from their label generously — over-reserving costs a little whitespace,
 * under-reserving would let a wide label collide with a neighbor.
 *
 * Shared with the headless layout tests, which is the point: the invariants
 * hold for exactly the footprints the app lays out with.
 */

const GLYPH_DIMS: Record<string, { w: number; h: number }> = {
  entrypoint: { w: 24, h: 24 },
  exit: { w: 24, h: 24 },
  nop: { w: 32, h: 32 },
  cycle: { w: 40, h: 40 },
  sleep: { w: 40, h: 40 },
  startThread: { w: 40, h: 40 },
  waitForThreads: { w: 40, h: 40 },
  runChildWf: { w: 40, h: 40 },
  waitForChildWf: { w: 40, h: 40 },
  startMultipleThreads: { w: 40, h: 40 },
}

/** `<index>-<label>-<TYPE>` -> `<label>`; tolerant of unexpected shapes. */
const labelOf = (nodeId: string): string => {
  const parts = nodeId.split('-')
  return parts.length >= 3 ? parts.slice(1, -1).join('-') : nodeId
}

export const nodeDimensions = (type: string | undefined, nodeId: string): { w: number; h: number } => {
  const glyph = GLYPH_DIMS[type ?? '']
  if (glyph) return glyph
  // Boxed nodes (task, userTask, externalEvent, throwEvent, waitForCondition,
  // ...): icon + text-xs label with padding; ~8.5px per character is a safe
  // upper bound for the 12px font.
  const w = Math.min(280, Math.max(110, Math.round(labelOf(nodeId).length * 8.5 + 56)))
  return { w, h: 50 }
}
