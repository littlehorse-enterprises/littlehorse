'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Edge as ReactFlowEdge } from 'reactflow';
import { useEdgeDataEditor } from '../../hooks/useEdgeDataEditor';

export function EdgeDataPanel({ edge }: { edge: ReactFlowEdge }) {
  const { handleDelete } = useEdgeDataEditor(edge.id);

  return (
    <Card className="border-gray-700 bg-gray-900 text-white">
      <CardHeader>
        <CardTitle className="text-sm">Edge Data Panel</CardTitle>
      </CardHeader>
      <CardContent>
        <Button onClick={handleDelete}>Delete</Button>
      </CardContent>
    </Card>
  )
}
