import { type Node, type Edge, MarkerType } from '@xyflow/react';

// Mock data for 2 nodes and 1 edge
export const mockNodes: Node[] = [
  {
    id: 'start-task',
    data: { 
      label: 'Start Task',
      status: 'completed'
    },
    type: 'task',
    position: { x: 0, y: 0 },
  },
  {
    id: 'end-task',
    data: { 
      label: 'End Task',
      status: 'pending'
    },
    type: 'task',
    position: { x: 0, y: 0 },
  },
];

export const mockEdges: Edge[] = [
  {
    id: 'start-task->end-task',
    source: 'start-task',
    target: 'end-task',
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
];

// Additional mock data for future use
export const extendedMockNodes: Node[] = [
  {
    id: 'start',
    data: { 
      label: 'Start Process',
      status: 'completed'
    },
    type: 'task',
    position: { x: 0, y: 0 },
  },
  {
    id: 'validate',
    data: { 
      label: 'Validate Input',
      status: 'running'
    },
    type: 'task',
    position: { x: 0, y: 0 },
  },
  {
    id: 'process',
    data: { 
      label: 'Process Data',
      status: 'pending'
    },
    type: 'task',
    position: { x: 0, y: 0 },
  },
  {
    id: 'complete',
    data: { 
      label: 'Complete',
      status: 'pending'
    },
    type: 'task',
    position: { x: 0, y: 0 },
  },
];

export const extendedMockEdges: Edge[] = [
  {
    id: 'start->validate',
    source: 'start',
    target: 'validate',
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
  {
    id: 'validate->process',
    source: 'validate',
    target: 'process',
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
  {
    id: 'process->complete',
    source: 'process',
    target: 'complete',
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
]; 