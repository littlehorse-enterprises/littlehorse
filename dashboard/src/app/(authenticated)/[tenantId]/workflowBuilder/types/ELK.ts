import type { Node as ReactFlowNode, Edge as ReactFlowEdge } from 'reactflow';

export type LayoutDirection = 'DOWN' | 'RIGHT';

export interface ElkLayoutOptions {
  'elk.algorithm'?: string;
  'elk.direction'?: LayoutDirection;
  'elk.layered.spacing.nodeNodeBetweenLayers'?: string;
  'elk.spacing.nodeNode'?: string;
}

export interface LayoutResult {
  nodes: ReactFlowNode[];
  edges: ReactFlowEdge[];
}
