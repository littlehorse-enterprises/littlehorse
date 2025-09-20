'use client';

import { Panel } from 'reactflow';
import { Button } from '@/components/ui/button';
import type { LayoutDirection } from '../types';

interface LayoutPanelProps {
  handleLayout: (direction: LayoutDirection) => void;
}

export function LayoutPanel({ handleLayout }: LayoutPanelProps) {
  return (
    <Panel position="top-right" className="flex gap-2 bg-gray-900 px-2 py-3">
      <Button onClick={() => handleLayout('DOWN')}>Vertical Layout</Button>
      <Button onClick={() => handleLayout('RIGHT')}>Horizontal Layout</Button>
    </Panel>
  )
}
